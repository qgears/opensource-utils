package hu.qgears.parser.util;

import java.util.Arrays;

/**
 * Dynamically extending integer array.
 * Internal int[] array is extended by doubling when necessary.
 * Values can be set by overwriting existing values or by adding a new value to the current end of the array.
 * @author rizsi
 *
 */
final public class UtilIntArrayFlexible {
	private int[] array = new int[initialsize];
	/**
	 * The number of valid entries in the int array currently.
	 */
	private int length = 0;
	public static final int initialsize = 5;
	public static final int multiplier = 2;

	/**
	 * Get the value at the given index.
	 * @param pos
	 * @return value at the index.
	 * @throws IndexOutOfBoundsException in case the position is not available in this flexible array.
	 */
	public int get(int pos) {
		if (pos >= length || pos < 0) {
			throw new IndexOutOfBoundsException("felxible int array: " + length
					+ " " + pos);
		}
		return array[pos];
	}

	/**
	 * Set value at the given index.
	 * 
	 * Values can be set by overwriting existing values or by adding a new value to the current end of the array.
	 * 
	 * @param pos index to set value at.
	 * @param value new value to set at the index.
	 * @return self
	 */
	public UtilIntArrayFlexible set(int pos, int value) {
		if (pos < 0) {
			throw new IndexOutOfBoundsException("felxible int array set: "
					+ pos);
		}else if(pos>length)
		{
			throw new IndexOutOfBoundsException("values can only be added to the end of the felxible int array! Add to pos:"
					+ pos+" current length: "+length);
		}
		checkResizeArray(pos);
		array[pos] = value;
		length = Math.max(pos + 1, length);
		return this;
	}

	private void checkResizeArray(int pos) {
		if(pos>=array.length)
		{
			int newSize = Math.max(array.length * multiplier, pos + 1);
			array=Arrays.copyOf(array, newSize);
		}
	}

	/**
	 * Nothing to do.
	 */
	public void clear() {
	}
}
