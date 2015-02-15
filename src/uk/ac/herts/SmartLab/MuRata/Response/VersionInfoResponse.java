package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.CMDCode;

public class VersionInfoResponse extends Payload {
	public VersionInfoResponse(Payload payload) {
		super(payload);
	}

	public CMDCode GetStatus() {
		return CMDCode.parse(this.GetData()[2] & 0xFF);
	}

	public byte GetVersionStringLength() {
		return this.GetData()[3];
	}

	public String GetVsersionString() {
		int size = this.GetVersionStringLength();
		return new String(this.GetData(), 4, size);
	}
}
