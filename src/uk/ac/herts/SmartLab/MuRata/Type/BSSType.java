package uk.ac.herts.SmartLab.MuRata.Type;

public enum BSSType {
	Infrastructure(0x00), AD_Hoc(0x01), Any(0x02);

	private int value;

	BSSType(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}

	public static BSSType parse(int value) {
		switch (value) {
		case 0x00:
			return Infrastructure;
		case 0x01:
			return AD_Hoc;
		case 0x02:
			return Any;
		}

		return null;
	}
}