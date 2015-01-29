package uk.ac.herts.SmartLab.MuRata.Type;

public enum CommandID {
	NAK(0x00),

	// / <summary>
	// / General Management
	// / </summary>
	CMD_ID_GEN(0x01),

	// / <summary>
	// / WIFI API
	// / </summary>
	CMD_ID_WIFI(0x50),

	// / <summary>
	// / SNIC API
	// / </summary>
	CMD_ID_SNIC(0x70),

	ACK(0x7F);

	private int value;

	CommandID(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}

	public static CommandID parse(int value) {
		switch (value) {
		case 0x00:
			return NAK;
		case 0x01:
			return CMD_ID_GEN;
		case 0x50:
			return CMD_ID_WIFI;
		case 0x70:
			return CMD_ID_SNIC;
		case 0x7F:
			return ACK;
		}

		return null;
	}
}
