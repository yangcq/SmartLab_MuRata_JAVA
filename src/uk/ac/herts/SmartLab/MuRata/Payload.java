package uk.ac.herts.SmartLab.MuRata;

import uk.ac.herts.SmartLab.MuRata.Type.*;

/*
 * All SNIC commands have the following format for the Payload field of the frame:
 * UINT8 Response Flag and Sub-Command ID (RFSCID)
 * … Rest of payload depends on RFSCID
 * The first byte of the Payload of the command describes the specific operation to perform for the Command ID, and it contains the Response Flag and Sub-Command ID (RFSCID). The Payload may be a request (REQ) from the host to the module, a response (RSP) from the module to that request, an indication (IND) from the module to the host, and the optional confirmation (CFM) from the host for that indication.
 */
public class Payload {
	private static final int EXPANDSIZE = 1024;

	private byte[] data;

	private int position;

	public Payload() {
		this.data = new byte[EXPANDSIZE];
		position = 0;
	}

	public Payload(Payload payload) {
		this.data = payload.data;
		this.position = payload.position;
	}

	public byte[] GetData() {
		return this.data;
	}

	// / <summary>
	// / The first byte of the Payload of the command describes the specific
	// operation to perform for the Command ID, and it contains the Response
	// Flag and Sub-Command ID (RFSCID). The Payload may be a request (REQ) from
	// the host to the module, a response (RSP) from the module to that request,
	// an indication (IND) from the module to the host, and the optional
	// confirmation (CFM) from the host for that indication.
	// / </summary>
	// / <returns></returns>
	public ResponseFlag GetResponseFlag() {
		if ((data[0] >> 7) == 0x01)
			return ResponseFlag.Response_or_Confirmation;
		else
			return ResponseFlag.Request_or_Indication;
	}

	public void SetResponseFlag(ResponseFlag flag) {
		if (flag == ResponseFlag.Request_or_Indication)
			data[0] &= 0x7F;
		else
			data[0] |= 0x80;
	}

	// sub command id
	public int GetSubCommandID() {
		return data[0] & 0x7F;
	}

	// / <summary>
	// / must call SetSubCommandID first then SetFrameID, and any other data
	// afterwards
	// / </summary>
	public void SetSubCommandID(SubCommandID id) {
		this.SetSubCommandID(id.getValue());
	}

	// / <summary>
	// / must call SetSubCommandID first then SetFrameID, and any other data
	// afterwards
	// / </summary>
	public void SetSubCommandID(int value) {
		data[0] = (byte) ((data[0] & 0x80) | (value & 0x7F));
		this.position = 1;
	}

	// frame id
	public byte GetFrameID() {
		return data[1];
	}

	// / <summary>
	// / must call SetSubCommandID first then SetFrameID, and any other data
	// afterwards
	// / </summary>
	public void SetFrameID(byte frameID) {
		data[1] = frameID;
		this.position = 2;
	}

	// content
	public int GetPosition() {
		return this.position;
	}

	public void SetPosition(int position) {
		if (position > this.data.length)
			this.position = this.data.length;
		else
			this.position = position;
	}

	public void Allocate(int length) {
		if (length <= 0)
			return;

		if (length > this.data.length)
			this.data = new byte[length];

		this.Rewind();
	}

	public void Rewind() {
		this.position = 0;
	}

	public void SetContent(byte value) {
		if (this.position >= this.data.length) {
			byte[] temp = this.data;
			this.data = new byte[this.data.length + Payload.EXPANDSIZE];
			System.arraycopy(temp, 0, this.data, 0, this.position);
		}

		this.data[position++] = value;
	}

	public void SetContent(byte[] value) {
		SetContent(value, 0, value.length);
	}

	public void SetContent(byte[] value, int offset, int length) {
		if (this.position + length - offset >= this.data.length) {
			byte[] temp = this.data;
			this.data = new byte[this.data.length + EXPANDSIZE
					* (1 + length / EXPANDSIZE)];
			System.arraycopy(temp, 0, this.data, 0, this.position);
		}

		System.arraycopy(value, 0, data, position, length);
		position += length;
	}
}
