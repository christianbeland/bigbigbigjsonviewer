package data;


public class TextPart implements IDataPart {

	private String data;
	private long byteOffset;

	public TextPart(String data, long byteOffset) {
		this.data = data;
		this.byteOffset = byteOffset;
	}

	@Override
	public String toString() {
		return data;
	}

	@Override
	public long getByteOffset() {
		return byteOffset;
	}
}
