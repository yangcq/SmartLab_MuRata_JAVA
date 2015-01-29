package uk.ac.herts.SmartLab.MuRata.Type;

public enum ScanType {
	Active_Scan(0x00), Passive_Scan(0x01);

	private int value;

	ScanType(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}
}
