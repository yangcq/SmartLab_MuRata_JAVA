﻿package uk.ac.herts.SmartLab.MuRata.Config;

public class UARTConfig implements IConfig {
	public enum FlowControl {
		NoFlowControl(0x00), HardwareFlowControl(0x20);
		private int value;

		FlowControl(int value) {
			this.value = value;
		}

		public byte getValue() {
			return (byte) value;
		}
	}

	public enum Parity {
		None(0x00), Odd(0x40), Even(0x80);

		private int value;

		Parity(int value) {
			this.value = value;
		}

		public byte getValue() {
			return (byte) value;
		}
	}

	public enum StopBits {
		StopBit1(0x01), StopBit2(0x02);

		private int value;

		StopBits(int value) {
			this.value = value;
		}

		public byte getValue() {
			return (byte) value;
		}
	}

	public enum BaudRate {
		_9600(9600), _19200(19200), _38400(38400), _57600(57600), _115200(
				115200), _230400(230400), _460800(460800), _921600(921600);

		private int value;

		BaudRate(int value) {
			this.value = value;
		}

		public byte getValue() {
			return (byte) value;
		}
	}

	/*
	 * Parameters are as follows: UINT8 Request Sequence UINT8 Port UINT8 Format
	 * UINT32 Baud Port specifies the UART port. Valid value is 1.
	 */

	// Data 0x10 8 data bits
	private byte[] value = new byte[] { 0x01, 0x10, 0x00, 0x00, 0x00, 0x00 };

	public byte[] GetValue() {
		return this.value;
	}

	public UARTConfig(FlowControl flowControl, Parity parity,
			StopBits StopBits, BaudRate baudRate) {
		this.SetFlowControl(flowControl).SetParity(parity)
				.SetStopBits(StopBits).SetBaudRate(baudRate);
	}

	public UARTConfig SetFlowControl(FlowControl flowControl) {
		this.value[1] = (byte) (this.value[1] & 0xDF | flowControl.value);
		return this;
	}

	public UARTConfig SetParity(Parity parity) {
		this.value[1] = (byte) (this.value[1] & 0x3F | parity.value);
		return this;
	}

	public UARTConfig SetStopBits(StopBits stop) {
		this.value[1] = (byte) (this.value[1] & 0xFC | stop.value);
		return this;
	}

	public UARTConfig SetBaudRate(BaudRate baudRate) {
		value[2] = (byte) (baudRate.value >> 24);
		value[3] = (byte) (baudRate.value >> 16);
		value[4] = (byte) (baudRate.value >> 8);
		value[5] = (byte) (baudRate.value);
		return this;
	}
}
