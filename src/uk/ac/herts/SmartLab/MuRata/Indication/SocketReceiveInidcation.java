package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;

/// <summary>
/// This event is generated when a TCP server or a UDP server (in connected mode) receives a packet. Since there is no client address and port information, the application may need to call
/// </summary>
public class SocketReceiveInidcation extends Payload {
	public static final int PAYLOAD_OFFSET = 5;

	private int receiveLength;

	public SocketReceiveInidcation(Payload payload) {
		super(payload);
		receiveLength = this.GetData()[3] << 8 | this.GetData()[4];
	}

	public byte GetServerSocketID() {
		return this.GetData()[2];
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
