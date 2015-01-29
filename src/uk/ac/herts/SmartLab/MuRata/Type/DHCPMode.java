package uk.ac.herts.SmartLab.MuRata.Type;

public enum DHCPMode {
	static_IP(0x00), dynamic_IP(0x01), soft_AP(0x02);

	private int value;

	DHCPMode(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}

	public static DHCPMode parse(int value) {
		switch (value) {
		case 0x00:
			return static_IP;
		case 0x01:
			return dynamic_IP;
		case 0x02:
			return soft_AP;
		}
		return null;
	}
}