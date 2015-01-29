package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.SNICCode;

public class SocketStartReceiveResponse extends Payload {
	public SocketStartReceiveResponse(Payload payload) {
		super(payload);
	}

	public SNICCode GetStatus() {
		return SNICCode.parse(this.GetData()[2]);
	}

	public int GetReceiveBufferSize() {
		if (GetStatus() == SNICCode.SNIC_SUCCESS)
			return this.GetData()[3] << 8 | this.GetData()[4];

		return -1;
	}
}
