package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.Type.*;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.WIFICode;

public class WIFIConnectionIndication extends Payload {
	public WIFIConnectionIndication(Payload payload) {
		super(payload);
	}

	public WIFIInterface GetInterface() {
		return WIFIInterface.parse(this.GetData()[2]);
	}

	public WIFICode GetStatus() {
		return WIFICode.parse(this.GetData()[3]);
	}

	public String GetSSID() {
		int _position = 4;
		int start = 4;

		while (this.GetData()[_position++] != 0x00) {
		}

		return new String(this.GetData(), start, _position - start - 1);
	}
}