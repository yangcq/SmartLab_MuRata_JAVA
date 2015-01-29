package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.SNICCode;

public class CreateSocketResponse extends Payload {
	public CreateSocketResponse(Payload payload) {
		super(payload);
	}

	public SNICCode GetStatus() {
		return SNICCode.parse(this.GetData()[2]);
	}

	public byte GetSocketID() {
		return this.GetData()[3];
	}
}