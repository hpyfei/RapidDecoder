package agu.bitmap.png.chunks;

import agu.bitmap.png.ImageInfo;

/**
 * Placeholder for UNKNOWN (custom or not) chunks.
 * <p>
 * For PngReader, a chunk is unknown if it's not registered in the chunk factory
 */
public class PngChunkUNKNOWN extends PngChunkMultiple { // unkown, custom or not

	public PngChunkUNKNOWN(String id, ImageInfo info) {
		super(id, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.NONE;
	}

	@Override
	public ChunkRaw createRawChunk() {
		return raw;
	}

	@Override
	public void parseFromRaw(ChunkRaw c) {

	}

	/* does not do deep copy! */
	public byte[] getData() {
		return raw.data;
	}

	/* does not do deep copy! */
	public void setData(byte[] data) {
		raw.data = data;
	}

	@Override
	public PngChunk cloneForWrite(ImageInfo imgInfo) {
		PngChunkUNKNOWN other = new PngChunkUNKNOWN(id, imgInfo);
		return other;
	}
}
