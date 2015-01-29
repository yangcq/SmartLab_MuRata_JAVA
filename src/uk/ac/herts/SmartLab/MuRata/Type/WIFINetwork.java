package uk.ac.herts.SmartLab.MuRata.Type;

public class WIFINetwork extends WIFIInfo {
	private byte keylength;
	private byte[] key;
	private byte[] BSSID;

	public byte GetSecurityLength() {
		return keylength;
	}

	public byte[] GetSecurityKey() {
		return key;
	}

	public byte[] GetBSSID() {
		return BSSID;
	}

	public WIFINetwork() {
	}

	public WIFINetwork(String SSID, SecurityMode securityMode) throws Exception {
		this(SSID, securityMode, null);
	}

	public WIFINetwork(String SSID, SecurityMode securityMode,
			String securityKey) throws Exception {
		super(SSID, securityMode);
		SetSecurityKey(securityKey);
	}

	public WIFINetwork SetSecurityKey(String SecurityKey) throws Exception {
		if (SecurityKey != null) {
			this.key = SecurityKey.getBytes();
			this.keylength = (byte) this.key.length;

			if (this.keylength >= 64)
				throw new Exception("UINT8 Security key length (0-64)");
		} else
			keylength = 0;
		return this;
	}

	public WIFINetwork SetBSSID(byte[] BSSID) throws Exception {
		if (BSSID.length != 6)
			throw new Exception("BSSID must be 6 bytes");

		this.BSSID = BSSID;
		return this;
	}

	@Override
	public WIFINetwork SetSSID(String SSID) throws Exception {
		super.SetSSID(SSID);
		return this;
	}

	@Override
	public WIFINetwork SetSecurityMode(SecurityMode securityMode) {
		super.SetSecurityMode(securityMode);
		return this;
	}

	@Override
	public WIFINetwork SetChannel(byte channel) {
		super.SetChannel(channel);
		return this;
	}
}
