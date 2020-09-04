package commoble.exmachinaessentials.content.solar_panel;

import java.util.Set;
import java.util.function.BiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.Constants;

public class SolarPanelBlock extends Block
{
	public static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

	public SolarPanelBlock(Properties props)
	{
		super(props);
		this.setDefaultState(this.stateContainer.getBaseState().with(POWER, 0));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(POWER);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	// "isOpaque"? daylight detector returns true here
	@Override
	public boolean func_220074_n(BlockState state)
	{
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(POWER, getLightLevel(context.getWorld(), context.getPos()));
	}
	
	/**
	 * Returns the integer daylight level of the position, same math as daylight detector
	 * @param world world
	 * @param pos pos
	 * @return A value in the range [0,15]
	 */
	public static int getLightLevel(IWorld iworld, BlockPos pos)
	{
		if (iworld.getDimension().hasSkyLight() && iworld instanceof World)
		{
			World world = (World)iworld;
			int light = world.getLightLevel(LightType.SKY, pos) - world.getSkylightSubtracted();

			if (light > 0)
			{
				float celestialAngle = world.getCelestialAngleRadians(1.0F);
				float angleBias = celestialAngle < (float) Math.PI ? 0.0F : ((float) Math.PI * 2F);
				celestialAngle = celestialAngle + (angleBias - celestialAngle) * 0.2F;
				light = Math.round(light * MathHelper.cos(celestialAngle));
			}

			return MathHelper.clamp(light, 0, 15);
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	@Deprecated
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		doPostSetOperation(world, pos, Set<BlockPos>::add);
		super.onBlockAdded(state, world, pos, oldState, isMoving);
	}

	@Override
	@Deprecated
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			doPostSetOperation(world, pos, Set<BlockPos>::remove);
		}
		super.onReplaced(state, world, pos, newState, isMoving);
	}
	
	public static void doPostSetOperation(World world, BlockPos pos, BiConsumer<Set<BlockPos>, BlockPos> consumer)
	{
		IChunk chunk = world.getChunk(pos);
		if (chunk instanceof Chunk)
		{
			((Chunk)chunk).getCapability(SolarPanelsInChunkCapability.INSTANCE)
				.ifPresent(posts -> consumer.accept(posts.getPositions(), pos));
		}
	}
	
	public static void tickSolarPanels(ISolarPanelsInChunk panels, IWorld world)
	{
		panels.getPositions().forEach(pos -> tickSolarPanel(pos, world));
	}
	
	public static void tickSolarPanel(BlockPos pos, IWorld world)
	{
		BlockState state = world.getBlockState(pos);
		if (state.contains(SolarPanelBlock.POWER))
		{
			world.setBlockState(pos, state.with(SolarPanelBlock.POWER, SolarPanelBlock.getLightLevel(world, pos)), Constants.BlockFlags.DEFAULT);
		}
	}
}
