package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.Type.WIFIStatusCode;

public class WIFIStatusResponse extends Payload {
	public WIFIStatusResponse(Payload payload) {
		super(payload);
	}

	public WIFIStatusCode GetWiFiStatusCode() {
		return WIFIStatusCode.parse(this.GetData()[2] & 0xFF);
	}

	// / <summary>
	// / Present only if WiFi Status code is not WIFI_OFF.
	// / </summary>
	// / <returns></returns>
	public byte[] GetMACAddress() {
		if (this.GetWiFiStatusCode() == WIFIStatusCode.WIFI_OFF)
			return null;

		byte[] value = new byte[6];
		System.arraycopy(this.GetData(), 3, value, 0, 6);
		return value;
	}

	// / <summary>
	// / Present only if WiFi Status code is STA_JOINED or AP_STARTED.
	// / </summary>
	// / <returns></returns>
	public String GetSSID() {
		WIFIStatusCode code = GetWiFiStatusCode();
		if (code == WIFIStatusCode.STA_JOINED
				|| code == WIFIStatusCode.AP_STARTED)
			return new String(this.GetData(), 9, this.GetPosition() - 10);

		return null;
	}
}