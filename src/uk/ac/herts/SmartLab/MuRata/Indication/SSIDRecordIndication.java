package uk.ac.herts.SmartLab.MuRata.Indication;

import java.util.ArrayList;

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

		int _position = 3;

		ArrayList<WIFINetworkDetail> list = new ArrayList<WIFINetworkDetail>();

		byte[] value = this.GetData();

		while (_position < this.GetPosition()) {
			WIFINetworkDetail detail = new WIFINetworkDetail();

			try {
				detail.SetChannel(value[_position++])
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

				detail.SetSSID(new String(this.GetData(), start, _position
						- start - 1));

				list.add(detail);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		WIFINetworkDetail[] container = new WIFINetworkDetail[list.size()];
		return list.toArray(container);
	}
}
