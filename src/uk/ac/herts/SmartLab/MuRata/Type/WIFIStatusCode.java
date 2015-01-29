package uk.ac.herts.SmartLab.MuRata.Type;

public enum WIFIStatusCode {
	WIFI_OFF, NO_NETWORK, STA_JOINED, AP_STARTED;

	public static WIFIStatusCode parse(int value) {
		switch (value) {
		case 0x00:
			return WIFI_OFF;
		case 0x01:
			return NO_NETWORK;
		case 0x02:
			return STA_JOINED;
		case 0x03:
			return AP_STARTED;
		}
		return null;
	}
}
