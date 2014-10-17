package by.dev.madhead.lzwj.util;

import java.util.Arrays;

/**
 * Array of bytes.
 * 
 * @author madhead
 * 
 */
public class ByteArray {
	private byte[] internal;

	/**
	 * Default no-arg constructor. Initializes zero length byte array.
	 */
	public ByteArray() {
		internal = new byte[0];
	}

	/**
	 * Constructs <code>ByteArray</code> from <code>another</code>.
	 * 
	 * @param another
	 */
	public ByteArray(ByteArray another) {
		internal = another.internal.clone();
	}

	/**
	 * Constructs <code>ByteArray</code> from array of bytes.
	 * 
	 * @param array
	 */
	public ByteArray(byte[] array) {
		internal = array.clone();
	}

	/**
	 * Constructs <code>ByteArray</code> from array of bytes.
	 * 
	 * @param b1
	 * @param bytes
	 */
	public ByteArray(byte b1, byte... bytes) {
		int bytesSize = (bytes != null) ? bytes.length : 0;

		internal = new byte[bytesSize + 1];
		internal[0] = b1;
		for (int i = 1; i < internal.length; i++) {
			internal[i] = bytes[i - 1];
		}
	}

	/**
	 * Returns size of this array.
	 * 
	 * @return size of this array.
	 */
	public int size() {
		return internal.length;
	}

	/**
	 * Gets specified byte from array.
	 * 
	 * @param index
	 *            index of byte to retreive.
	 * @return specified byte from array.
	 */
	public byte get(int index) {
		return internal[index];
	}

	/**
	 * Sets specified byte in array.
	 * 
	 * @param index
	 *            index of byte to set.
	 * @param value
	 *            byte to set.
	 * 
	 */
	public void set(int index, byte value) {
		internal[index] = value;
	}

	/**
	 * Appends all bytes from <code>another</code> to this byte array.
	 * 
	 * @param another
	 *            <code>ByteArray</code> to append to this one.
	 * @return this
	 */
	public ByteArray append(ByteArray another) {
		int size = size();
		int anotherSize = another.size();
		int newSize = size + anotherSize;
		byte[] newBuf = new byte[newSize];

		for (int i = 0; i < size; i++) {
			newBuf[i] = get(i);
		}
		for (int i = 0; i < anotherSize; i++) {
			newBuf[i + size] = another.get(i);
		}
		internal = newBuf;
		return this;
	}

	/**
	 * Appends all bytes from <code>array</code> to this byte array.
	 * 
	 * @param array
	 *            bytes to append to this one.
	 * @return this
	 */
	public ByteArray append(byte[] array) {
		return append(new ByteArray(array));
	}

	/**
	 * Appends bytes to this byte array.
	 * 
	 * @param b1
	 * @param bytes
	 * @return this
	 */
	public ByteArray append(byte b1, byte... bytes) {
		return append(new ByteArray(b1, bytes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ByteArray other = (ByteArray) obj;
		if (!Arrays.equals(internal, other.internal))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(internal);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ByteArray [internal=" + Arrays.toString(internal) + "]";
	}
}
