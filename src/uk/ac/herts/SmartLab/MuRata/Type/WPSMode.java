package uk.ac.herts.SmartLab.MuRata.Type;

public enum WPSMode {
	Push_Button(0x00), Pin(0x01);
	
	private int value;

	WPSMode(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}
}