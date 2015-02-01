package uk.ac.herts.SmartLab.MuRata.Indication;

import uk.ac.herts.SmartLab.MuRata.Payload;
import uk.ac.herts.SmartLab.MuRata.Type.*;

public class SSIDRecordIndication extends Payload {
	public SSIDRecordIndication(Payload payload) {
		super(payload);
	}

	public int GetNumberofRecords() {
		return this.GetData()[2];
	}

	public WIFINetworkDetail[] GetRecords() {
		int count = this.GetNumberofRecords();

		if (count <= 0)
			return null;

		int index = 0;
		int _position = 3;

		WIFINetworkDetail[] list = new WIFINetworkDetail[count];

		byte[] value = this.GetData();

		while (_position < this.GetPosition()) {
			list[index] = new WIFINetworkDetail();

			try {
				list[index]
						.SetChannel(value[_position++])
						.SetRSSI(value[_position++])
						.SetSecurityMode(SecurityMode.parse(value[_position++]))
						.SetBSSID(
								new byte[] { value[_position++],
										value[_position++], value[_position++],
										value[_position++], value[_position++],
										value[_position++] })
						.SetNetworkType(BSSType.parse(value[_position++]))
						.SetMaxDataRate(value[_position++]);

				_position++;

				int start = _position;
				while (value[_position++] != 0x00) {
				}

				list[index].SetSSID(new String(this.GetData(), start, _position
						- start - 1));

				index++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return list;
	}
}
