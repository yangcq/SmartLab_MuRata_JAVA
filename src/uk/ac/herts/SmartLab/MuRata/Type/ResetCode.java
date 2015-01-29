package uk.ac.herts.SmartLab.MuRata.Type;

public enum ResetCode {
	Window_Watchdog_Reset, Independent_Watchdog_Reset, Software_Reset, POR_PDR_Reset, Pin_Reset;

	public static ResetCode parse(int value) {
		switch (value) {
		case 0x4000:
			return Window_Watchdog_Reset;
		case 0x2000:
			return Independent_Watchdog_Reset;
		case 0x1000:
			return Software_Reset;
		case 0x0800:
			return POR_PDR_Reset;
		case 0x0400:
			return Pin_Reset;
		}

		return null;
	}
}