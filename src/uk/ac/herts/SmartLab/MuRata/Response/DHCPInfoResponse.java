package uk.ac.herts.SmartLab.MuRata.Response;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.SNICCode;
import uk.ac.herts.SmartLab.MuRata.Type.*;

public class DHCPInfoResponse extends Payload {
	public DHCPInfoResponse(Payload payload) {
		super(payload);
	}

	public SNICCode GetStatus() {
		return SNICCode.parse(this.GetData()[2] & 0xFF);
	}

	public byte[] GetLocalMAC() {
		if (this.GetStatus() != SNICCode.SNIC_SUCCESS)
			return null;

		return new byte[] { this.GetData()[3], this.GetData()[4],
				this.GetData()[5], this.GetData()[6], this.GetData()[7],
				this.GetData()[8] };
	}

	public IPAddress GetLocalIP() {
		if (this.GetStatus() != SNICCode.SNIC_SUCCESS)
			return null;

		return new IPAddress(this.GetData(), 9);
	}

	public IPAddress GetGatewayIP() {
		if (this.GetStatus() != SNICCode.SNIC_SUCCESS)
			return null;

		return new IPAddress(this.GetData(), 13);
	}

	public IPAddress GetSubnetMask() {
		if (this.GetStatus() != SNICCode.SNIC_SUCCESS)
			return null;

		return new IPAddress(this.GetData(), 17);
	}

	public DHCPMode GetDHCPMode() {
		return DHCPMode.parse(this.GetData()[21] & 0xFF);
	}
}
