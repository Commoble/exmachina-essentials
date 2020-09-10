package commoble.exmachinaessentials.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import commoble.exmachinaessentials.content.turbine.SteamTurbineContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SteamTurbineScreen extends ContainerScreen<SteamTurbineContainer>
{
	public static final ResourceLocation TEXTURE = new ResourceLocation("exmachinaessentials:textures/gui/container/steam_turbine.png");
	
	public static final int BACKGROUND_WIDTH = 176;
	public static final int BACKGROUND_HEIGHT = 166;
	
	public static final int FLAME_SOURCE_X = BACKGROUND_WIDTH;
	public static final int FLAME_SOURCE_Y = 0;
	public static final int FLAME_DEST_X = 56;
	public static final int FLAME_DEST_Y = 36;
	public static final int FLAME_WIDTH = 13;
	public static final int FLAME_HEIGHT = 13;
	
	public static final int BOLT_SOURCE_X = BACKGROUND_WIDTH;
	public static final int BOLT_SOURCE_Y = 14;
	public static final int BOLT_DEST_X = 114;
	public static final int BOLT_DEST_Y = 32;
	public static final int BOLT_WIDTH = 17;
	public static final int BOLT_HEIGHT = 21;
	
	public SteamTurbineScreen(SteamTurbineContainer container, PlayerInventory playerInventory, ITextComponent title)
	{
		super(container, playerInventory, title);
		// standard width and height
		this.xSize = BACKGROUND_WIDTH;
		this.ySize = BACKGROUND_HEIGHT;
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		this.drawMouseoverTooltip(matrix, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		// draw the background
		this.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		this.drawTexture(matrix, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		
		// draw progress bars
		int burnRemaining = this.container.getBurnTimeRemaining();
		if (burnRemaining > 0)
		{
			int burnAmount = this.container.getBurnLeftScaled() + 1;
			this.drawTexture(matrix,
				this.guiLeft + FLAME_DEST_X,
				this.guiTop + FLAME_DEST_Y + FLAME_HEIGHT - burnAmount,
				FLAME_SOURCE_X,
				FLAME_SOURCE_Y + FLAME_HEIGHT - burnAmount,
				FLAME_WIDTH,
				burnAmount);
			
			this.drawTexture(matrix, this.guiLeft + BOLT_DEST_X, this.guiTop + BOLT_DEST_Y, BOLT_SOURCE_X, BOLT_SOURCE_Y, BOLT_WIDTH, BOLT_HEIGHT);
		}
	}

}
