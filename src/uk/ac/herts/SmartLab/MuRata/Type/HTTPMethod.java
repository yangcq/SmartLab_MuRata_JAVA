package uk.ac.herts.SmartLab.MuRata.Type;

public enum HTTPMethod {
	GET(0x00), POST(0x01);

	private int value;

	HTTPMethod(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}
}
