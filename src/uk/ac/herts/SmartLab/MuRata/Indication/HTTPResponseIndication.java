package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;

/// <summary>
/// The most significant bit of Content length is reserved to indicate if there is more data to send to the host. When this bit is 1, the host application should continue to receive SNIC_HTTP_RSP_IND, until this bit is 0. The Content length is limited by the receive buffer size specified in SNIC_INIT_REQ and the system resource at that moment.
/// </summary>
public class HTTPResponseIndication extends Payload {
	public static final int PAYLOAD_OFFSET = 4;

	private int contentLength;

	public HTTPResponseIndication(Payload payload) {
		super(payload);
		contentLength = (this.GetData()[2] & 0x7F) << 8 | this.GetData()[3];
	}

	public boolean isMoreDataComing() {
		return (this.GetData()[2] >> 7) == 0x01 ? true : false;
	}

	public int GetContentLength() {
		return this.contentLength;
	}

	public byte GetContent(int index) {
		return this.GetData()[index + PAYLOAD_OFFSET];
	}

	public int GetContentOffset() {
		return PAYLOAD_OFFSET;
	}
}