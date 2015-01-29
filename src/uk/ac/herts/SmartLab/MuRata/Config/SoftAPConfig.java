package uk.ac.herts.SmartLab.MuRata.Config;

import uk.ac.herts.SmartLab.MuRata.Type.*;

public class SoftAPConfig extends WIFINetwork {
	/*
	 * Parameters are as follows: UINT8 Request Sequence UINT8 Onoff UINT8
	 * Persistency UINT8 SSID [up to 33] UINT8 Channel UINT8 Security mode UINT8
	 * Security key length (0-64) … Security key [ ] OnOff = 0 indicates AP is
	 * to be turned off. The rest of the parameters are ignored. OnOff = 1
	 * indicates turning on soft AP using existing NVM parameters, OnOff = 2
	 * indicates turning on AP with the parameters provided. If the soft AP is
	 * already on, it is first turned off. Persistency=1 indicates the soft AP’s
	 * on/off state and parameters (if OnOff = 2) will be saved in NVM. For
	 * example, if OnOff =0 and Persistency=1, the soft AP will not be turned on
	 * after a reset.
	 */

	public enum State {
		// / <summary>
		// / indicates AP is to be turned off. The rest of the parameters are
		// ignored.
		// / </summary>
		OFF(0x00),

		// / <summary>
		// / indicates turning on soft AP using existing NVM parameters,
		// / </summary>
		ON_NVM(0x01),

		// / <summary>
		// / indicates turning on AP with the parameters provided. If the soft
		// AP is already on, it is first turned off.
		// / </summary>
		ON_PARAMETERS(0x02);
		private int value;

		State(int value) {
			this.value = value;
		}

		public byte getValue() {
			return (byte) value;
		}
	}

	private State onOff;
	private boolean persistency;

	public SoftAPConfig(State state) throws Exception {
		this(state, "", SecurityMode.WIFI_SECURITY_OPEN, null);
	}

	// / <summary>
	// / OnOff = 0 indicates AP is to be turned off. The rest of the parameters
	// are ignored.
	// / BSSID is not required
	// / !!! cannot be WEP and WIFI_SECURITY_WPA_AES_PSK !!!
	// / </summary>
	// / <param name="SSID">only required when OnOff = 2, which is
	// ON_PARAMETERS</param>
	// / <param name="securityMode"></param>
	// / <param name="securityKey"></param>
	public SoftAPConfig(State state, String SSID, SecurityMode securityMode,
			String securityKey) throws Exception {
		super(SSID, securityMode, securityKey);
		this.SetOnOffState(state);
	}

	public byte GetOnOffStatus() {
		return onOff.getValue();
	}

	public byte GetPersistency() {
		return (byte) (this.persistency ? 0x01 : 0x00);
	}

	public SoftAPConfig SetOnOffState(State onOff) {
		this.onOff = onOff;
		return this;
	}

	public SoftAPConfig SetPersistency(boolean persistency) {
		this.persistency = persistency;
		return this;
	}

	@Override
	public SoftAPConfig SetSecurityKey(String SecurityKey) throws Exception {
		super.SetSecurityKey(SecurityKey);
		return this;
	}

	@Override
	public SoftAPConfig SetBSSID(byte[] BSSID) throws Exception {
		super.SetBSSID(BSSID);
		return this;
	}

	@Override
	public SoftAPConfig SetSSID(String SSID) throws Exception {
		super.SetSSID(SSID);
		return this;
	}

	@Override
	public SoftAPConfig SetSecurityMode(SecurityMode securityMode) {
		super.SetSecurityMode(securityMode);
		return this;
	}

	@Override
	public SoftAPConfig SetChannel(byte channel) {
		super.SetChannel(channel);
		return this;
	}
}
