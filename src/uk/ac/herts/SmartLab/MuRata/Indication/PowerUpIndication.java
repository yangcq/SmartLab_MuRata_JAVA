package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.Type.ResetCode;

public class PowerUpIndication extends Payload {
	public PowerUpIndication(Payload payload) {
		super(payload);
	}

	public ResetCode GetResetCode() {
		return ResetCode.parse((this.GetData()[2] << 8) | this.GetData()[3]);
	}
}
