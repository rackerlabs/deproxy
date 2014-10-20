package by.dev.madhead.lzwj.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.dev.madhead.lzwj.io.Input;
import by.dev.madhead.lzwj.io.Output;
import by.dev.madhead.lzwj.util.ByteArray;

/**
 * Class for compressing and decompressing. It is not thread-safe.
 * 
 * @author madhead
 * 
 */
public class LZW {
	/**
	 * Initial size of compress table. Before starting LZW compression each byte
	 * has its mapping set to itself.
	 */
	public static final int INITIAL_DICT_SIZE = 256;

	/**
	 * Size in bits of compressed code.
	 */
	public static final int DEFAULT_CODEWORD_LENGTH = 12;

	private int codeWordLength = DEFAULT_CODEWORD_LENGTH;
	private Map<ByteArray, Integer> codeTable;
	private List<ByteArray> decodeTable;

	/**
	 * Returns currently used codeword length.
	 * 
	 * @return codeword length.
	 */
	public int getCodeWordLength() {
		return codeWordLength;
	}

	/**
	 * Sets codeword length in bits to be used in compress/decompress
	 * operations.
	 * 
	 * @param codeWordLength
	 *            codeword length in bits to be used in compress/decompress
	 *            operations.
	 */
	public void setCodeWordLength(int codeWordLength) {
		// Haha! Expected this method do something useful, didn't you?
		// this.codeWordLength = codeWordLength;
	}

	/**
	 * Compresses <code>in</code> to <code>out</code>. Flushes output after
	 * completion. You must explicitly close this streams after compression.
	 * 
	 * @param in
	 *            input stream to be compressed.
	 * @param out
	 *            output stream to place compression result in.
	 * @throws java.io.IOException
	 */
	public void compress(InputStream in, OutputStream out) throws IOException {
		// Here be dragons!
		init();

		int code = INITIAL_DICT_SIZE;
		int maxCode = (1 << codeWordLength) - 1;

		InputStream bufferedIn = new BufferedInputStream(in);
		Output compressedOutput = new Output(new BufferedOutputStream(out),
				codeWordLength);

		int firstByte = bufferedIn.read();
		ByteArray w = new ByteArray((byte) firstByte);
		int K;

		while ((K = bufferedIn.read()) != -1) {
			ByteArray wK = new ByteArray(w).append((byte) K);
			if (codeTable.containsKey(wK)) {
				w = wK;
			} else {
				compressedOutput.write(codeTable.get(w));
				if (code < maxCode) {
					codeTable.put(wK, code++);
				}
				w = new ByteArray((byte) K);
			}
		}
		compressedOutput.write(codeTable.get(w));
		compressedOutput.flush();
	}

	/**
	 * Decompresses <code>in</code> to <code>out</code>. Flushes output after
	 * completion. You must explicitly close this streams after compression.
	 * 
	 * @param in
	 *            input stream to be decompressed.
	 * @param out
	 *            output stream to place decompression result in.
	 * @throws java.io.IOException
	 */
	public void decompress(InputStream in, OutputStream out) throws IOException {
		init();

		Input compressedInput = new Input(new BufferedInputStream(in),
				codeWordLength);
		OutputStream bufferedOut = new BufferedOutputStream(out);

		int oldCode = compressedInput.read();
		bufferedOut.write(oldCode);
		int character = oldCode;
		int newCode;
		while ((newCode = compressedInput.read()) != -1) {
			ByteArray string;
			if (newCode >= decodeTable.size()) {
				string = new ByteArray(decodeTable.get(oldCode));
				string.append((byte) character);
			} else {
				string = decodeTable.get(newCode);
			}
			for (int i = 0; i < string.size(); i++) {
				bufferedOut.write(string.get(i));
			}
			character = string.get(0);
			decodeTable.add(new ByteArray(decodeTable.get(oldCode))
					.append((byte) character));
			oldCode = newCode;
		}

		bufferedOut.flush();
	}

	/**
	 * Initializes class for compression and decompression.
	 */
	private void init() {
		codeTable = new HashMap<ByteArray, Integer>();
		decodeTable = new ArrayList<ByteArray>();
		for (int i = 0; i < INITIAL_DICT_SIZE; i++) {
			codeTable.put(new ByteArray((byte) i), i);
			decodeTable.add(new ByteArray((byte) i));
		}
	}
}
