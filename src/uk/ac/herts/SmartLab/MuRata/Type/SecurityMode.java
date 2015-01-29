package uk.ac.herts.SmartLab.MuRata.Type;

public enum SecurityMode {
	WIFI_SECURITY_OPEN(0x00), WEP(0x01), WIFI_SECURITY_WPA_TKIP_PSK(0x02), WIFI_SECURITY_WPA2_AES_PSK(
			0x04), WIFI_SECURITY_WPA2_MIXED_PSK(0x06), WIFI_SECURITY_WPA_AES_PSK(
			0x07), NOT_SUPPORTED(0xFF);

	private int value;

	SecurityMode(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}

	public static SecurityMode parse(int value) {
		switch (value) {
		case 0x00:
			return WIFI_SECURITY_OPEN;
		case 0x01:
			return WEP;
		case 0x02:
			return WIFI_SECURITY_WPA_TKIP_PSK;
		case 0x04:
			return WIFI_SECURITY_WPA2_AES_PSK;
		case 0x06:
			return WIFI_SECURITY_WPA2_MIXED_PSK;
		case 0x07:
			return WIFI_SECURITY_WPA_AES_PSK;
		case 0xFF:
			return NOT_SUPPORTED;
		}
		return null;
	}
}
