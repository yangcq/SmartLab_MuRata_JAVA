package uk.ac.herts.SmartLab.MuRata.Type;

public enum SocketSentOption {
	no_action(0x00), shutdown_socket_both_directions(0x01), close_socket(0x02);

	private int value;

	SocketSentOption(int value) {
		this.value = value;
	}

	public byte getValue() {
		return (byte) value;
	}
}