package commoble.exmachinaessentials.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

/**
 * 
The MIT License (MIT)

Copyright (c) 2019 Joseph Bettendorff aka "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

/**
 * Helper class for converting lists or list-like collections of arbitrary data to NBT and back.
 * 
 * @param ENTRY the data type in the actual java list, i.e. List<ENTRY>
 * @param RAW either a primitive or an NBT collection type, see ListNBTType instances
 */
public class NBTListCodec<ENTRY, RAW>
{
	private final String name;
	private final ListNBTType<RAW> type;
	private final Function<ENTRY, INBT> elementWriter;
	private final Function<RAW, ENTRY> elementReader;
	
	/**
	 * 
	 * @param name A string to use as the field name when we write the list into existing nbt
	 * @param type A ListNBTType that describes how to get data out of the list and convert primitives to NBT
	 * @param elementWriter A function that converts an entry instance to the intermediate raw type
	 * @param elementReader A function that converts the raw instance back into an entry instance
	 */
	public NBTListCodec(
			String name,
			ListNBTType<RAW> type,
			Function<ENTRY, RAW> elementWriter,
			Function<RAW, ENTRY> elementReader)
	{
		this.name = name;
		this.type = type;
		this.elementWriter = elementWriter.andThen(this.type.serializer);
		this.elementReader = elementReader;
	}
	
	/**
	 * Reconstructs and returns a List<T> from a CompoundNBT
	 * If the nbt used was given by this.write(list), the list returned will be a reconstruction of the original List
	 * @param compound The CompoundNBT to read and construct the List from
	 * @return A List that the data contained in the CompoundNBT represents
	 */
	public List<ENTRY> read(final CompoundNBT compound)
	{
		final List<ENTRY> newList = new ArrayList<>();
		
		final ListNBT listNBT = compound.getList(this.name, this.type.tagID);
		if (listNBT == null)
			return newList;
		
		final int listSize = listNBT.size();
		
		if (listSize <= 0)
			return newList;
		
		IntStream.range(0, listSize).mapToObj(i -> this.type.listReader.apply(listNBT, i))
			.forEach(nbt -> newList.add(this.elementReader.apply(nbt)));
		
		return newList;
	}
	
	/**
	 * Given a list and a CompoundNBT,writes the contents of that list into the NBT
	 * The same CompoundNBT can be given to this.read to retrieve that map 
	 * @param list A List<T> to write into the nbt
	 * @param compound A CompoundNBT to write the list into
	 * @return A CompoundNBT that, when used as the argument to this.read(nbt), will cause that function to reconstruct and return a copy of the original list
	 */
	public CompoundNBT write(final List<ENTRY> list, final CompoundNBT compound)
	{
		final ListNBT nbtList = new ListNBT();
		
		list.forEach(element -> nbtList.add(this.elementWriter.apply(element)));
		
		compound.put(this.name, nbtList);
		
		return compound;
	}
	
	public static class ListNBTType<RAW>
	{
		public static final ListNBTType<Byte> BYTE = new ListNBTType<>(Constants.NBT.TAG_BYTE, (list, i) -> (byte)(list.getInt(i)), ByteNBT::of);
		public static final ListNBTType<Short> SHORT = new ListNBTType<>(Constants.NBT.TAG_SHORT, ListNBT::getShort, ShortNBT::of);
		public static final ListNBTType<Integer> INTEGER = new ListNBTType<>(Constants.NBT.TAG_INT, ListNBT::getInt, IntNBT::of);
		//public static final NBTType<Long> LONG = new NBTType<>(Constants.NBT.TAG_LONG, lists do not have long getter, LongNBT::valueOf);
		public static final ListNBTType<Float> FLOAT = new ListNBTType<>(Constants.NBT.TAG_FLOAT, ListNBT::getFloat, FloatNBT::of);
		public static final ListNBTType<Double> DOUBLE = new ListNBTType<>(Constants.NBT.TAG_DOUBLE, ListNBT::getDouble, DoubleNBT::of);
		public static final ListNBTType<String> STRING = new ListNBTType<>(Constants.NBT.TAG_STRING, ListNBT::getString, StringNBT::of);
		public static final ListNBTType<ListNBT> LIST = new ListNBTType<>(Constants.NBT.TAG_LIST, ListNBT::getList, x->x);
		public static final ListNBTType<CompoundNBT> COMPOUND = new ListNBTType<>(Constants.NBT.TAG_COMPOUND, ListNBT::getCompound, x->x);
		
		/** see forge's Constants.NBT, needed for ListNBTs to work safely **/
		final int tagID;
		final BiFunction<ListNBT, Integer, RAW> listReader;
		final Function<RAW, INBT> serializer;
		
		public ListNBTType(int tagID, BiFunction<ListNBT, Integer, RAW> listReader, Function<RAW, INBT> serializer)
		{
			this.tagID = tagID;
			this.listReader = listReader;
			this.serializer = serializer;
		}
	}
}