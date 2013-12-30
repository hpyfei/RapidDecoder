package agu.bitmap.png;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 * Some utility static methods for internal use.
 * <p>
 * Client code should not normally use this class
 * <p>
 */
public final class PngHelperInternal {

	public static final String KEY_LOGGER = "ar.com.pngj";
	public static final Logger LOGGER = Logger.getLogger(KEY_LOGGER);

	/**
	 * Default charset, used internally by PNG for several things
	 */
	public static String charsetLatin1name = "ISO-8859-1";
	public static Charset charsetLatin1 = Charset.forName(charsetLatin1name);
	/**
	 * UTF-8 is only used for some chunks
	 */
	public static String charsetUTF8name = "UTF-8";
	public static Charset charsetUTF8 = Charset.forName(charsetUTF8name);

	static boolean DEBUG = false;

	/**
	 * PNG magic bytes
	 */
	public static byte[] getPngIdSignature() {
		return new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };
	}

	public static int doubleToInt100000(double d) {
		return (int) (d * 100000.0 + 0.5);
	}

	public static double intToDouble100000(int i) {
		return i / 100000.0;
	}

	public static int readByte(InputStream is) {
		try {
			return is.read();
		} catch (IOException e) {
			throw new PngjInputException("error reading byte", e);
		}
	}

	/**
	 * -1 if eof
	 * 
	 * PNG uses "network byte order"
	 */
	public static int readInt2(InputStream is) {
		try {
			int b1 = is.read();
			int b2 = is.read();
			if (b1 == -1 || b2 == -1)
				return -1;
			return (b1 << 8) | b2;
		} catch (IOException e) {
			throw new PngjInputException("error reading Int2", e);
		}
	}

	/**
	 * -1 if eof
	 */
	public static int readInt4(InputStream is) {
		try {
			int b1 = is.read();
			int b2 = is.read();
			int b3 = is.read();
			int b4 = is.read();
			if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1)
				return -1;
			return (b1 << 24) | (b2 << 16) | (b3 << 8) + b4;
		} catch (IOException e) {
			throw new PngjInputException("error reading Int4", e);
		}
	}

	public static int readInt1fromByte(byte[] b, int offset) {
		return (b[offset] & 0xff);
	}

	public static int readInt2fromBytes(byte[] b, int offset) {
		return ((b[offset] & 0xff) << 8) | ((b[offset + 1] & 0xff));
	}

	public static final int readInt4fromBytes(byte[] b, int offset) {
		return ((b[offset] & 0xff) << 24) | ((b[offset + 1] & 0xff) << 16) | ((b[offset + 2] & 0xff) << 8)
				| (b[offset + 3] & 0xff);
	}

	public static void writeInt2tobytes(int n, byte[] b, int offset) {
		b[offset] = (byte) ((n >> 8) & 0xff);
		b[offset + 1] = (byte) (n & 0xff);
	}

	public static void writeInt4tobytes(int n, byte[] b, int offset) {
		b[offset] = (byte) ((n >> 24) & 0xff);
		b[offset + 1] = (byte) ((n >> 16) & 0xff);
		b[offset + 2] = (byte) ((n >> 8) & 0xff);
		b[offset + 3] = (byte) (n & 0xff);
	}

	/**
	 * guaranteed to read exactly len bytes. throws error if it can't
	 */
	public static void readBytes(InputStream is, byte[] b, int offset, int len) {
		if (len == 0)
			return;
		try {
			int read = 0;
			while (read < len) {
				int n = is.read(b, offset + read, len - read);
				if (n < 1)
					throw new PngjInputException("error reading bytes, " + n + " !=" + len);
				read += n;
			}
		} catch (IOException e) {
			throw new PngjInputException("error reading", e);
		}
	}

	public static void skipBytes(InputStream is, long len) {
		try {
			while (len > 0) {
				long n1 = is.skip(len);
				if (n1 > 0) {
					len -= n1;
				} else if (n1 == 0) { // should we retry? lets read one byte
					if (is.read() == -1) // EOF
						break;
					else
						len--;
				} else
					// negative? this should never happen but...
					throw new IOException("skip() returned a negative value ???");
			}
		} catch (IOException e) {
			throw new PngjInputException(e);
		}
	}

	public static void logdebug(String msg) {
		if (DEBUG)
			System.out.println(msg);
	}

	// / filters
	public static int filterRowNone(int r) {
		return (int) (r & 0xFF);
	}

	public static int filterRowSub(int r, int left) {
		return ((int) (r - left) & 0xFF);
	}

	public static int filterRowUp(int r, int up) {
		return ((int) (r - up) & 0xFF);
	}

	public static int filterRowAverage(int r, int left, int up) {
		return (r - (left + up) / 2) & 0xFF;
	}

	public static int filterRowPaeth(int r, int left, int up, int upleft) { // a = left, b = above, c = upper left
		return (r - filterPaethPredictor(left, up, upleft)) & 0xFF;
	}

	final static int filterPaethPredictor(final int a, final int b, final int c) { // a = left, b = above, c = upper
																					// left
		// from http://www.libpng.org/pub/png/spec/1.2/PNG-Filters.html

		final int p = a + b - c;// ; initial estimate
		final int pa = p >= a ? p - a : a - p;
		final int pb = p >= b ? p - b : b - p;
		final int pc = p >= c ? p - c : c - p;
		// ; return nearest of a,b,c,
		// ; breaking ties in order a,b,c.
		if (pa <= pb && pa <= pc)
			return a;
		else if (pb <= pc)
			return b;
		else
			return c;
	}

	/*
	 * we put this methods here so as to not pollute the public interface of PngReader
	 */
	final public static void initCrcForTests(PngReader pngr) {
		if (pngr.idatCrc == null)
			pngr.idatCrc = new CRC32();
	}

	final public static long getCrctestVal(PngReader pngr) {
		if (pngr.idatCrc != null)
			return pngr.idatCrc.getValue();
		else
			return -1;
	}

	/**
	 * Prits a debug message (prints class name, method and line number)
	 * 
	 * @param obj
	 *            : Object to print
	 */
	static void debug(Object obj) {
		debug(obj, 1, true);
	}

	/**
	 * Prits a debug message (prints class name, method and line number)
	 * 
	 * @param obj
	 *            : Object to print
	 * @param offset
	 *            : Offset N lines from stacktrace
	 */
	static void debug(Object obj, int offset) {
		debug(obj, offset, true);
	}

	static InputStream istreamFromFile(File f) {
		FileInputStream is;
		try {
			is = new FileInputStream(f);
		} catch (Exception e) {
			throw new PngjInputException("Could not open " + f, e);
		}
		return is;
	}

	/**
	 * Prints a debug message (prints class name, method and line number) to
	 * stderr and logFile
	 * 
	 * @param obj
	 *            : Object to print
	 * @param offset
	 *            : Offset N lines from stacktrace
	 * @param newLine
	 *            : Print a newline char at the end ('\n')
	 */
	static void debug(Object obj, int offset, boolean newLine) {
		StackTraceElement ste = new Exception().getStackTrace()[1 + offset];
		String steStr = ste.getClassName();
		int ind = steStr.lastIndexOf('.');
		steStr = steStr.substring(ind + 1);
		steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "): " + (obj == null ? null : obj.toString());
		System.out.println(steStr);
	}

}
