package uk.ac.herts.SmartLab.MuRata.Type;

public abstract class WIFIInfo {
	private byte channel;
	private String ssid;
	private byte[] _ssid;
	private SecurityMode mode;

	public byte[] GetSSID() {
		return _ssid;
	}

	public String GetSSIDasString() {
		return ssid;
	}

	public SecurityMode GetSecurityMode() {
		return mode;
	}

	public byte GetChannel() {
		return channel;
	}

	public WIFIInfo() {
	}

	public WIFIInfo(String SSID, SecurityMode securityMode) throws Exception {
		this.SetSSID(SSID).SetSecurityMode(securityMode);
	}

	public WIFIInfo SetSSID(String SSID) throws Exception {
		this._ssid = SSID.getBytes();

		if (this._ssid.length >= 33)
			throw new Exception("UINT8 SSID [Up to 32 octets]");

		this.ssid = SSID;

		return this;
	}

	public WIFIInfo SetSecurityMode(SecurityMode securityMode) {
		this.mode = securityMode;
		return this;
	}

	public WIFIInfo SetChannel(byte channel) {
		this.channel = channel;
		return this;
	}

	@Override
	public String toString() {
		return this.ssid;
	}
}
