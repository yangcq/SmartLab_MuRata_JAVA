package uk.ac.herts.SmartLab.MuRata.Type;

import uk.ac.herts.SmartLab.MuRata.Config.IConfig;

public class IPAddress implements IConfig {
	public static final IPAddress ANY = new IPAddress(new byte[] { 0x00, 0x00,
			0x00, 0x00 });
	public static final IPAddress Loopback = new IPAddress(new byte[] { 0x7F,
			0x00, 0x00, 0x01 });

	private byte[] address = new byte[4];

	public IPAddress(String ip) throws Exception {
		String[] ips = ip.split(".");
		if (ips.length != 4)
			throw new Exception("IP Address : X.X.X.X");

		address[0] = Byte.parseByte(ips[0]);
		address[1] = Byte.parseByte(ips[1]);
		address[2] = Byte.parseByte(ips[2]);
		address[3] = Byte.parseByte(ips[3]);
	}

	public IPAddress(byte[] data, int offset) {
		System.arraycopy(data, offset, this.address, 0, 4);
	}

	public IPAddress(byte[] data) {
		System.arraycopy(data, 0, this.address, 0, 4);
	}

	public byte[] GetValue() {
		return this.address;
	}

	@Override
	public String toString() {
		return address[0] + "." + address[1] + "." + address[2] + "."
				+ address[3];
	}
}