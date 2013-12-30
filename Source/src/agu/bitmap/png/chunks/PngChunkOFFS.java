package agu.bitmap.png.chunks;

import agu.bitmap.png.ImageInfo;
import agu.bitmap.png.PngHelperInternal;
import agu.bitmap.png.PngjException;

/**
 * oFFs chunk.
 * <p>
 * see http://www.libpng.org/pub/png/spec/register/pngext-1.3.0-pdg.html#C.oFFs
 */
public class PngChunkOFFS extends PngChunkSingle {
	public final static String ID = "oFFs";

	// http://www.libpng.org/pub/png/spec/register/pngext-1.3.0-pdg.html#C.oFFs
	private long posX;
	private long posY;
	private int units; // 0: pixel 1:micrometer

	public PngChunkOFFS(ImageInfo info) {
		super(ID, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.BEFORE_IDAT;
	}

	@Override
	public void parseFromRaw(ChunkRaw chunk) {
		if (chunk.len != 9)
			throw new PngjException("bad chunk length " + chunk);
		posX = PngHelperInternal.readInt4fromBytes(chunk.data, 0);
		if (posX < 0)
			posX += 0x100000000L;
		posY = PngHelperInternal.readInt4fromBytes(chunk.data, 4);
		if (posY < 0)
			posY += 0x100000000L;
		units = PngHelperInternal.readInt1fromByte(chunk.data, 8);
	}

	@Override
	public PngChunk cloneForWrite(ImageInfo imgInfo) {
		PngChunkOFFS other = new PngChunkOFFS(imgInfo);
		other.posX = posX;
		other.posY = posY;
		other.units = units;
		return other;
	}

	/**
	 * 0: pixel, 1:micrometer
	 */
	public int getUnits() {
		return units;
	}

	/**
	 * 0: pixel, 1:micrometer
	 */
	public void setUnits(int units) {
		this.units = units;
	}

	public long getPosX() {
		return posX;
	}

	public void setPosX(long posX) {
		this.posX = posX;
	}

	public long getPosY() {
		return posY;
	}

	public void setPosY(long posY) {
		this.posY = posY;
	}

}
