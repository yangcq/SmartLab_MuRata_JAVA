package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.Type.*;
/// <summary>
/// This event is generated when a UDP server (in unconnected mode) receives a packet.
/// </summary>

public class UDPReceivedIndication extends Payload {
	public static final int PAYLOAD_OFFSET = 11;

	private int receiveLength;

	public UDPReceivedIndication(Payload payload) {
		super(payload);
		receiveLength = this.GetData()[9] << 8 | this.GetData()[10];
	}

	public byte GetServerSocketID() {
		return this.GetData()[2];
	}

	public IPAddress GetRemoteIP() {
		return new IPAddress(this.GetData(), 3);
	}

	public int GetRemotePort() {
		return this.GetData()[7] << 8 | this.GetData()[8];
	}

	public int GetPayloadLength() {
		return this.receiveLength;
	}

	public byte GetPayload(int index) {
		return this.GetData()[index + PAYLOAD_OFFSET];
	}

	public int GetPayloadOffset() {
		return PAYLOAD_OFFSET;
	}

}