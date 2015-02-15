package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.SNICCode;

/// <summary>
/// This event describes the status of a network connection (identified by a socket)
/// </summary>
public class TCPStatusIndication extends Payload {
	public TCPStatusIndication(Payload payload) {
		super(payload);
	}

	public SNICCode GetStatus() {
		return SNICCode.parse(this.GetData()[2] & 0xFF);
	}

	public byte GetSocketID() {
		return this.GetData()[3];
	}
}
