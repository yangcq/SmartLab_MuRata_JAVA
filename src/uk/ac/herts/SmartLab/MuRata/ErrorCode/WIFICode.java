package uk.ac.herts.SmartLab.MuRata.ErrorCode;

public enum WIFICode {
	WIFI_NORESPONSE, WIFI_SUCCESS, WIFI_ERR_UNKNOWN_COUNTRY, WIFI_ERR_INIT_FAIL, WIFI_ERR_ALREADY_JOINED, WIFI_ERR_AUTH_TYPE, WIFI_ERR_JOIN_FAIL, WIFI_ERR_NOT_JOINED, WIFI_ERR_LEAVE_FAILED, WIFI_COMMAND_PENDING, WIFI_WPS_NO_CONFIG, WIFI_NETWORK_UP, WIFI_NETWORK_DOWN, WIFI_FAIL;

	public static WIFICode parse(int value) {
		switch (value) {
		case -1:
			return WIFI_NORESPONSE;
		case 0x00:
			return WIFI_SUCCESS;
		case 0x01:
			return WIFI_ERR_UNKNOWN_COUNTRY;
		case 0x02:
			return WIFI_ERR_INIT_FAIL;
		case 0x03:
			return WIFI_ERR_ALREADY_JOINED;
		case 0x04:
			return WIFI_ERR_AUTH_TYPE;
		case 0x05:
			return WIFI_ERR_JOIN_FAIL;
		case 0x06:
			return WIFI_ERR_NOT_JOINED;
		case 0x07:
			return WIFI_ERR_LEAVE_FAILED;
		case 0x08:
			return WIFI_COMMAND_PENDING;
		case 0x09:
			return WIFI_WPS_NO_CONFIG;
		case 0x10:
			return WIFI_NETWORK_UP;
		case 0x11:
			return WIFI_NETWORK_DOWN;
		case 0xFF:
			return WIFI_FAIL;
		}

		return null;
	}
}