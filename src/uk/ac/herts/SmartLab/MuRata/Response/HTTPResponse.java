package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;

public class HTTPResponse extends Payload {
	private int payloadOffset;

	private int contentLength;
	private int statusCode;
	private String contentType;

	public HTTPResponse(Payload payload) {
		super(payload);

		statusCode = this.GetData()[2] << 8 | this.GetData()[3];
		if (statusCode >= 100) {
			contentLength = (this.GetData()[4] & 0x7F) << 8 | this.GetData()[5];

			int _position = 6;
			int start = 6;

			while (this.GetData()[_position++] != 0x00) {
			}
			payloadOffset = _position;

			this.contentType = new String(this.GetData(), start, _position
					- start - 1);
		}
	}

	public boolean isMoreDataComing() {
		return (this.GetData()[4] >> 7) == 0x01 ? true : false;
	}

	public int GetContentLength() {
		return this.contentLength;
	}

	public int GetStatusCode() {
		return this.statusCode;
	}

	public byte GetContent(int index) {
		return this.GetData()[index + payloadOffset];
	}

	public String GetContentType() {
		return this.contentType;
	}

	public int GetContentOffset() {
		return this.payloadOffset;
	}
}
