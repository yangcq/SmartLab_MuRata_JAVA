package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.SNICCode;

public class InitializationResponse extends Payload {
	public InitializationResponse(Payload payload) {
		super(payload);
	}

	public SNICCode GetStatus() {
		return SNICCode.parse(this.GetData()[2]);
	}

	public int GetDefaultReceiveBufferSize() {
		return this.GetData()[3] << 8 | this.GetData()[4];
	}

	public int GetMaximumUDPSupported() {
		return this.GetData()[5];
	}

	public int GetMaximumTCPSupported() {
		return this.GetData()[6];
	}
}