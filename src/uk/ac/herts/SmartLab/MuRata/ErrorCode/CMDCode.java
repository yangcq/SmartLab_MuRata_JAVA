package uk.ac.herts.SmartLab.MuRata.ErrorCode;

public enum CMDCode {
	GEN_NORESPONSE, GEN_SUCCESS, GEN_FAILED;

	public static CMDCode parse(int value) {
		switch (value) {
		case -1:
			return GEN_NORESPONSE;
		case 0x00:
			return GEN_SUCCESS;
		case 0x01:
			return GEN_FAILED;
		}

		return null;
	}

}
