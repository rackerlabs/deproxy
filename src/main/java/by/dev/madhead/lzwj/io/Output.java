package by.dev.madhead.lzwj.io;

import java.io.IOException;
import java.io.OutputStream;

import by.dev.madhead.lzwj.util.Constants;
import by.dev.madhead.lzwj.util.Euclid;

/**
 * Class for writing codewords to streams. Handles codeword <-> byte converions.
 * 
 * @author madhead
 * 
 */
public class Output {
	/**
	 * Backing output stream.
	 */
	private OutputStream out;

	/**
	 * Length of code in bits.
	 */
	int codeWordLength;

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
	 * Number of codes currently written to the backing stream.
	 */
	private int written;

	/**
	 * Number of bits of internal buffer, actually used for storing codes.
	 */
	private int bufUsageBits;

	/**
	 * Number of bytes of internal buffer, actually used for storing codes.
	 * 
	 * @see bufUsageBits
	 */
	private int bufUsageBytes;

	/**
	 * Number of codes, that can be actually stored there.
	 */
	private int bufUsageSymbols;

	/**
	 * Constructor for Output.
	 * 
	 * @param out
	 *            backing stream to write bytes to.
	 * @param codeWordLength
	 *            size in bits of codewords, stored in <code>out</code>.
	 */
	public Output(OutputStream out, int codeWordLength) {
		this.out = out;
		this.codeWordLength = codeWordLength;

		written = 0;
		buf = 0;
		bufUsageBits = (int) Euclid.LCM(Constants.BITS_IN_BYTE, codeWordLength);
		bufUsageBytes = bufUsageBits / Constants.BITS_IN_BYTE;
		bufUsageSymbols = bufUsageBits / codeWordLength;
		mask = (1 << codeWordLength) - 1;
	}

	/**
	 * Writes codeword to backing stream.
	 * 
	 * @param code
	 *            codeword to write to backing stream.
	 * @throws java.io.IOException
	 */
	public void write(int code) throws IOException {
		// Did you know, that Java is big-endian?
		code = (code & mask) << ((written) * codeWordLength);
		buf |= code;
		written++;
		if (written >= bufUsageSymbols) {
			for (int i = 0; i < bufUsageBytes; i++) {
				out.write((int) (buf & Constants.BYTE_MASK));
				buf >>= Constants.BITS_IN_BYTE;
			}
			written = 0;
			buf = 0;
		}
	}

	/**
	 * Writes any buffered codewords to the stream and flushes it.
	 * 
	 * @throws java.io.IOException
	 */
	public void flush() throws IOException {
		while ((written < bufUsageSymbols) && (written != 0)) {
			write(-1);
		}
		out.flush();
	}

	/**
	 * Closes backing stream.
	 * 
	 * @throws java.io.IOException
	 */
	public void close() throws IOException {
		out.close();
	}
}
