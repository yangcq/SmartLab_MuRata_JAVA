package uk.ac.herts.SmartLab.MuRata.Type;

public class WIFINetworkDetail extends WIFINetwork {
	private int rssi;

	private BSSType netType;

	// Max Data Rate (Mbps)
	private int maxDataRate;

	public WIFINetworkDetail() {
	}

	public WIFINetworkDetail(String SSID, SecurityMode securityMode,
			BSSType networkType, int rssi, int maxDataRate) throws Exception {
		super(SSID, securityMode);
		this.netType = networkType;
		this.rssi = rssi;
		this.maxDataRate = maxDataRate;
	}

	public int GetRSSI() {
		return this.rssi;
	}

	// / <summary>
	// / Max Data Rate (Mbps)
	// / </summary>
	// / <returns></returns>
	public int GetMaxDataRate() {
		return this.maxDataRate;
	}

	public BSSType GetNetworkType() {
		return this.netType;
	}

	public WIFINetworkDetail SetRSSI(int rssi) {

		if (rssi >> 7 == 0x01)
			this.rssi = (~(rssi - 1) & 0x7F) * -1;
		else
			this.rssi = rssi;
		return this;
	}

	public WIFINetworkDetail SetNetworkType(BSSType networkType) {
		this.netType = networkType;
		return this;
	}

	public WIFINetworkDetail SetMaxDataRate(int maxDataRate) {
		this.maxDataRate = maxDataRate;
		return this;
	}

	public WIFINetworkDetail SetSecurityKey(String SecurityKey) throws Exception {
		super.SetSecurityKey(SecurityKey);
		return this;
	}

	public WIFINetworkDetail SetBSSID(byte[] BSSID) throws Exception {
		super.SetBSSID(BSSID);
		return this;
	}

	public WIFINetworkDetail SetSSID(String SSID) throws Exception {
		super.SetSSID(SSID);
		return this;
	}

	public WIFINetworkDetail SetSecurityMode(SecurityMode securityMode) {
		super.SetSecurityMode(securityMode);
		return this;
	}

	public WIFINetworkDetail SetChannel(byte channel) {
		super.SetChannel(channel);
		return this;
	}

}