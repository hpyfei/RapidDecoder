package agu.bitmap.png.chunks;

import agu.bitmap.png.ImageInfo;
import agu.bitmap.png.PngHelperInternal;
import agu.bitmap.png.PngjException;

/**
 * tRNS chunk.
 * <p>
 * see http://www.w3.org/TR/PNG/#11tRNS
 * <p>
 * this chunk structure depends on the image type
 */
public class PngChunkTRNS extends PngChunkSingle {
	public final static String ID = ChunkHelper.tRNS;

	// http://www.w3.org/TR/PNG/#11tRNS

	// only one of these is meaningful, depending on the image type
	private int gray;
	private int red, green, blue;
	private int[] paletteAlpha = new int[] {};

	public PngChunkTRNS(ImageInfo info) {
		super(ID, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT;
	}

	@Override
	public void parseFromRaw(ChunkRaw c) {
		if (imgInfo.greyscale) {
			gray = PngHelperInternal.readInt2fromBytes(c.data, 0);
		} else if (imgInfo.indexed) {
			int nentries = c.data.length;
			paletteAlpha = new int[nentries];
			for (int n = 0; n < nentries; n++) {
				paletteAlpha[n] = (int) (c.data[n] & 0xff);
			}
		} else {
			red = PngHelperInternal.readInt2fromBytes(c.data, 0);
			green = PngHelperInternal.readInt2fromBytes(c.data, 2);
			blue = PngHelperInternal.readInt2fromBytes(c.data, 4);
		}
	}

	@Override
	public PngChunk cloneForWrite(ImageInfo imgInfo) {
		PngChunkTRNS other = new PngChunkTRNS(imgInfo);
		other.gray = gray;
		other.red = red;
		other.green = green;
		other.blue = blue;
		if (paletteAlpha != null) {
			other.paletteAlpha = new int[paletteAlpha.length];
			System.arraycopy(paletteAlpha, 0, other.paletteAlpha, 0, other.paletteAlpha.length);
		}
		return other;
	}

	/**
	 * Set rgb values
	 * 
	 */
	public void setRGB(int r, int g, int b) {
		if (imgInfo.greyscale || imgInfo.indexed)
			throw new PngjException("only rgb or rgba images support this");
		red = r;
		green = g;
		blue = b;
	}

	public int[] getRGB() {
		if (imgInfo.greyscale || imgInfo.indexed)
			throw new PngjException("only rgb or rgba images support this");
		return new int[] { red, green, blue };
	}

	public int getRGB888() {
		if (imgInfo.greyscale || imgInfo.indexed)
			throw new PngjException("only rgb or rgba images support this");
		return (red << 16) | (green << 8) | blue;
	}

	public void setGray(int g) {
		if (!imgInfo.greyscale)
			throw new PngjException("only grayscale images support this");
		gray = g;
	}

	public int getGray() {
		if (!imgInfo.greyscale)
			throw new PngjException("only grayscale images support this");
		return gray;
	}

	/**
	 * WARNING: non deep copy
	 */
	public void setPalletteAlpha(int[] palAlpha) {
		if (!imgInfo.indexed)
			throw new PngjException("only indexed images support this");
		paletteAlpha = palAlpha;
	}

	/**
	 * to use when only one pallete index is set as totally transparent
	 */
	public void setIndexEntryAsTransparent(int palAlphaIndex) {
		if (!imgInfo.indexed)
			throw new PngjException("only indexed images support this");
		paletteAlpha = new int[] { palAlphaIndex + 1 };
		for (int i = 0; i < palAlphaIndex; i++)
			paletteAlpha[i] = 255;
		paletteAlpha[palAlphaIndex] = 0;
	}

	/**
	 * WARNING: non deep copy
	 */
	public int[] getPalletteAlpha() {
		return paletteAlpha;
	}

}
