package uk.ac.herts.SmartLab.MuRata.Type;

public enum WIFIInterface {
	STA(0x00), SoftAP(0x01);

	private int value;

	WIFIInterface(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}

	public static WIFIInterface parse(int value) {
		switch (value) {
		case 0x00:
			return STA;
		case 0x01:
			return SoftAP;
		}

		return null;
	}
}
