package by.dev.madhead.lzwj.io;

import java.io.IOException;
import java.io.InputStream;

import by.dev.madhead.lzwj.util.Constants;
import by.dev.madhead.lzwj.util.Euclid;

/**
 * Class for reading codewords from streams. Handles byte <-> codeword
 * converions.
 * 
 * @author madhead
 * 
 */
public class Input {
	/**
	 * Backing input stream.
	 */
	private InputStream in;

	/**
	 * Length of code in bits.
	 */
	private int codeWordLength;

	/**
	 * Binary mask for clipping <code>codeWordLength</code> bits from integers
	 * with binary operations.
	 */
	private int mask;

	/**
	 * Internal buffer for accumulating output.
	 */
	private long buf; // 8 bytes (64 bits), i hope

	/**
	 * Number of bits of internal buffer, actually used for storing codes.
	 */
	private int bufUsageBits;

	/**
	 * Number of bytes of internal buffer, actually used for storing codes.
	 */
	private int bufUsageBytes;

	/**
	 * Number of codes, that can be actually stored there.
	 */
	private int bufUsageSymbols;

	/**
	 * Number of codes actually stored in buf.
	 */
	private int bufferedCodes;

	/**
	 * End-of-file flag.
	 */
	private boolean eof;

	/**
	 * Constructor for Input.
	 * 
	 * @param in
	 *            backing stream to get bytes from.
	 * @param codeWordLength
	 *            size in bits of codewords, stored in <code>in</code>.
	 */
	public Input(InputStream in, int codeWordLength) {
		this.in = in;
		this.codeWordLength = codeWordLength;

		bufferedCodes = 0;
		buf = 0;
		bufUsageBits = (int) Euclid.LCM(Constants.BITS_IN_BYTE, codeWordLength);
		bufUsageBytes = bufUsageBits / Constants.BITS_IN_BYTE;
		bufUsageSymbols = bufUsageBits / codeWordLength;
		mask = (1 << codeWordLength) - 1;
	}

	/**
	 * Reads codeword from backing stream.
	 * 
	 * @return next codeword from stream or -1, if nothing more left there.
	 * @throws java.io.IOException
	 */
	public int read() throws IOException {
		if ((bufferedCodes <= 0) && (!eof)) {
			buf = 0;
			for (int i = 0; i < bufUsageBytes; i++) {
				int read = in.read();
				if (-1 == read) {
					// read = 0;
					eof = true;
				}
				read = read & Constants.BYTE_MASK;
				read <<= i * Constants.BITS_IN_BYTE;
				buf |= read;
			}
			bufferedCodes = bufUsageSymbols;
		}
		if (bufferedCodes > 0) {
			int code = (int) (buf & mask);
			buf >>= codeWordLength;
			bufferedCodes--;
			if (code < mask) {
				return code;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Closes backing stream.
	 * 
	 * @throws java.io.IOException
	 */
	public void close() throws IOException {
		in.close();
	}
}
