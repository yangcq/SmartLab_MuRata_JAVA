package uk.ac.herts.SmartLab.MuRata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import uk.ac.herts.SmartLab.MuRata.Config.*;
import uk.ac.herts.SmartLab.MuRata.ErrorCode.*;
import uk.ac.herts.SmartLab.MuRata.Indication.*;
import uk.ac.herts.SmartLab.MuRata.Response.*;
import uk.ac.herts.SmartLab.MuRata.Type.*;

public class MuRata {
	public static final int DEFAULT_BAUDRATE = 921600;
	private static final int DEFAULT_WAIT = 10000;

	boolean isSignal = false;

	private byte frameID = 0x00;

	private Payload _sendPayload, _receivePayload, _safePayload;
	private UARTFrame _sendFrame, _receiveFrame;

	private OutputStream outputStream;
	private InputStream inputStream;

	private ArrayList<MuRataIndicationListener> listeners = new ArrayList<MuRataIndicationListener>();

	public MuRata(OutputStream outputStream, InputStream inputStream) {
		_sendPayload = new Payload();
		_sendFrame = new UARTFrame();
		_receivePayload = new Payload();
		_receiveFrame = new UARTFrame();
		_receiveFrame.SetPayload(_receivePayload);
		_safePayload = new Payload();

		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	public void Start() {
		new Thread(DataReceiveThread).start();
	}

	public void Stop() {
	}

	public void AddIndicationListener(MuRataIndicationListener listener) {
		this.listeners.add(listener);
	}

	public void RemoveIndicationListener(MuRataIndicationListener listener) {
		this.listeners.remove(listener);
	}

	// region Send and Receive

	private void Send(boolean signal) {

		try {
			outputStream.write(UARTFrame.SOM);
			isSignal = signal;

			outputStream.write(_sendFrame.GetL0() | 0x80);
			outputStream.write(_sendFrame.GetL1() | 0x80
					| (_sendFrame.GetACKRequired() ? 0x40 : 0x00));

			outputStream.write(_sendFrame.GetCommandID().getValue() | 0x80);

			outputStream.write(_sendPayload.GetData(), 0,
					_sendPayload.GetPosition());

			outputStream.write(_sendFrame.GetChecksum() | 0x80);

			outputStream.write(UARTFrame.EOM);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// / <summary>
	// / return null means there is an error
	// / </summary>
	// / <returns>received UARTFrame</returns>
	private boolean FrameReceive() throws IOException {
		int value = inputStream.read();

		while (value != UARTFrame.SOM)
			value = inputStream.read();

		_receiveFrame.SetL0(inputStream.read());
		_receiveFrame.SetL1(inputStream.read());

		_receiveFrame.SetCommandID((byte) inputStream.read());

		int _size = _receiveFrame.GetPayloadLength();

		_receivePayload.Allocate(_size);
		while (_receivePayload.GetPosition() < _size)
			_receivePayload.SetPosition(_receivePayload.GetPosition()
					+ inputStream.read(_receivePayload.GetData(),
							_receivePayload.GetPosition(), _size
									- _receivePayload.GetPosition()));

		_receiveFrame.SetChecksum(inputStream.read());

		if (inputStream.read() == UARTFrame.EOM
				&& _receiveFrame.VerifyChecksum())
			return true;
		else
			return false;
	}

	private Runnable DataReceiveThread = new Runnable() {
		@Override
		public void run() {

			while (true) {
				try {
					if (FrameReceive() == false)
						continue;

					if (isSignal
							&& _receiveFrame.GetCommandID() == _sendFrame
									.GetCommandID()
							&& _receivePayload.GetSubCommandID() == _sendPayload
									.GetSubCommandID()
							&& _receivePayload.GetFrameID() == _sendPayload
									.GetFrameID()) {
						isSignal = false;
						_safePayload.Rewind();
						_safePayload.SetContent(_receivePayload.GetData(), 0,
								_receivePayload.GetPosition());

						synchronized (MuRata.this) {
							MuRata.this.notify();
						}
					} else {
						if (_receivePayload.GetResponseFlag() == ResponseFlag.Request_Indication) {
							switch (_receiveFrame.GetCommandID()) {
							case CMD_ID_GEN:
								GENIndication();
								break;
							case CMD_ID_WIFI:
								WIFIIndication();
								break;
							case CMD_ID_SNIC:
								SNICIndication();
								break;
							default:
								break;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	};

	private void GENIndication() {
		switch (_receivePayload.GetSubCommandID()) {
		case 0x00:// SubCommandID.GEN_PWR_UP_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onPowerUpIndication(new PowerUpIndication(
							_receivePayload));
			break;
		}
	}

	private void WIFIIndication() {
		switch (_receivePayload.GetSubCommandID()) {
		case 0x11: // SubCommandID.WIFI_SCAN_RESULT_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onScanResultIndication(new SSIDRecordIndication(
							_receivePayload));
			break;

		case 0x10: // SubCommandID.WIFI_NETWORK_STATUS_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onWIFIConnectionIndication(new WIFIConnectionIndication(
							_receivePayload));
			break;
		}
	}

	private void SNICIndication() {
		switch (_receivePayload.GetSubCommandID()) {
		case 0x20: // SubCommandID.SNIC_TCP_CONNECTION_STATUS_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onTcpConnectionStatusIndication(new TCPStatusIndication(
							_receivePayload));
			break;
		case 0x22: // SubCommandID.SNIC_CONNECTION_RECV_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onSocketReceiveIndication(new SocketReceiveInidcation(
							_receivePayload));
			break;
		case 0x23: // SubCommandID.SNIC_UDP_RECV_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onUDPReceiveIndication(new UDPReceivedIndication(
							_receivePayload));
			break;
		case 0x25: // SubCommandID.SNIC_HTTP_RSP_IND.getValue()
			if (listeners != null)
				for (MuRataIndicationListener listener : listeners)
					listener.onHTTPResponseIndication(new HTTPResponseIndication(
							_receivePayload));
			break;
		}
	}

	// region General Management

	public VersionInfoResponse GEN_GetFirmwareVersionInfo() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.GEN_FW_VER_GET_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_GEN);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new VersionInfoResponse(_safePayload);
	}

	public CMDCode GEN_RestoreNVMtoFactoryDefault() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.GEN_RESTORE_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_GEN);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return CMDCode.GEN_NORESPONSE;
		}

		return CMDCode.parse(_safePayload.GetData()[2]);
	}

	public CMDCode GEN_SoftReset() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.GEN_RESET_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_GEN);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return CMDCode.GEN_NORESPONSE;
		}

		return CMDCode.parse(_safePayload.GetData()[2]);
	}

	public CMDCode GEN_UARTConfiguration(UARTConfig config) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.GEN_UART_CFG_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(config.GetValue());

		_sendFrame.SetCommandID(CommandID.CMD_ID_GEN);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return CMDCode.GEN_NORESPONSE;
		}

		return CMDCode.parse(_safePayload.GetData()[2]);
	}

	// Command ID for WIFI

	public WIFICode WIFI_TurnOn() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_ON_REQ);
		_sendPayload.SetFrameID(frameID++);
		/*
		 * Country code is a 2-character ASCII string. E.g., “US” = the United
		 * States. For the complete list, see Appendix A. The default country
		 * code is “US”, which is one of the startup parameters in NVM. If the
		 * WIFI_ON_REQ has no intention of changing the country code, put 0x0000
		 * in the two-byte Country code, so that the firmware will use the
		 * country code configured in NVM.
		 */
		_sendPayload.SetContent((byte) 0x00);
		_sendPayload.SetContent((byte) 0x00);

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);

	}

	public WIFICode WIFI_TurnOff() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_OFF_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);

	}

	public WIFICode WIFI_SoftAPControl(SoftAPConfig config) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_AP_CTRL_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendPayload.SetContent(config.GetOnOffStatus());
		_sendPayload.SetContent(config.GetPersistency());
		if (config.GetOnOffStatus() == 0x02) {
			_sendPayload.SetContent(config.GetSSID());
			_sendPayload.SetContent((byte) 0x00);
		}
		_sendPayload.SetContent(config.GetChannel());
		_sendPayload.SetContent(config.GetSecurityMode().getValue());

		_sendPayload.SetContent(config.GetSecurityLength());
		if (config.GetSecurityMode() != SecurityMode.WIFI_SECURITY_OPEN
				&& config.GetSecurityLength() > 0)
			_sendPayload.SetContent(config.GetSecurityKey());

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);
	}

	public WIFICode WIFI_AssociateNetwork(WIFINetwork AP) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_JOIN_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(AP.GetSSID());
		_sendPayload.SetContent((byte) 0x00);

		_sendPayload.SetContent(AP.GetSecurityMode().getValue());
		_sendPayload.SetContent(AP.GetSecurityLength());
		if (AP.GetSecurityLength() > 0)
			_sendPayload.SetContent(AP.GetSecurityKey());

		if (AP.GetBSSID() != null) {
			_sendPayload.SetContent(AP.GetChannel());
			_sendPayload.SetContent(AP.GetBSSID());
		}

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);
	}

	public WIFICode WIFI_DisconnectNetwork() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_DISCONNECT_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);
	}

	public WIFIStatusResponse WIFI_GetStatus(WIFIInterface WiFiInterface) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_GET_STATUS_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(WiFiInterface.getValue());

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new WIFIStatusResponse(_safePayload);
	}

	// / <summary>
	// / This command requests the reporting of the current RSSI from module’s
	// STA interface
	// / </summary>
	// / <returns>RSSI in dBm. 127 means unspecified value</returns>
	public int WIFI_GetRSSI() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_GET_STA_RSSI_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return 127;
		}

		byte value = _safePayload.GetData()[2];

		if (value >> 7 == 0x01)
			return (~(_safePayload.GetData()[2] - 1) & 0x7F) * -1;

		return value;
	}

	public WIFICode WIFI_StartWPSProcess(WPSMode mode) throws Exception {
		return WIFI_StartWPSProcess(mode, null);
	}

	public WIFICode WIFI_StartWPSProcess(WPSMode mode, String Pin)
			throws Exception {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_WPS_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(mode.getValue());

		if (mode == WPSMode.Pin) {
			if (Pin == null)
				throw new Exception("Pin not present");

			_sendPayload.SetContent(Pin.getBytes());
			_sendPayload.SetContent((byte) 0x00);
		}

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);
	}

	public WIFICode WIFI_ScanNetworks(ScanType scan, BSSType bss) {
		try {
			return WIFI_ScanNetworks(scan, bss, null, null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	// / <summary>
	// / Upon a successful reception of the command, the module starts to scan.
	// The response will indicate only WIFI_SUCCESS if no error. Actual scan
	// result shall be sent from module as multiple indications defined in
	// WIFI_SCAN_RESULT_IND
	// / </summary>
	// / <param name="scan"></param>
	// / <param name="bss"></param>
	// / <param name="BSSID">6 bytes MAC address of the AP or STA.</param>
	// / <param name="channelList">up to 10 array elements</param>
	// / <param name="SSID">string for the AP or STA SSID, up to 32
	// bytes</param>
	// / <returns></returns>
	public WIFICode WIFI_ScanNetworks(ScanType scan, BSSType bss, byte[] BSSID,
			byte[] channelList, String SSID) throws Exception {
		if (BSSID != null && BSSID.length != 6)
			throw new Exception("BSSID: 6 bytes MAC address of the AP or STA.");

		if (channelList != null && channelList.length > 10)
			throw new Exception("Channel list: up to 10 array elements");

		byte[] _ssid = null;
		if (SSID != null) {
			_ssid = SSID.getBytes();
			if (_ssid.length > 32)
				throw new Exception("AP or STA SSID, up to 32 bytes.");
		}

		/*
		 * This command instructs the module to scan available networks.
		 * Parameters are as follows: UINT8 Request Sequence UINT8 Scan Type
		 * UINT8 BSS Type UINT8 BSSID [6] UINT8 Channel list [] UINT8 SSID[]
		 * BSSID, Channel List, and SSID are optional fields. All 0’s for BSSID,
		 * Channel list or SSID indicates it is not present. - Scan Type: 0 =
		 * Active scan, 1= Passive scan - BSS Type: 0 = Infrastructure, 1 = ad
		 * hoc, 2 = any - BSSID: 6 bytes MAC address of the AP or STA. 6 bytes
		 * of 0’s indicates it is not present. - Channel list: 0 terminated
		 * array, up to 10 array elements. - SSID: 0 terminated string for the
		 * AP or STA SSID, up to 33 bytes including NUL-termination.
		 */

		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.WIFI_SCAN_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(scan.getValue());
		_sendPayload.SetContent(bss.getValue());

		if (BSSID == null)
			_sendPayload.SetContent(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00,
					0x00 });
		else
			_sendPayload.SetContent(BSSID);

		if (channelList != null)
			_sendPayload.SetContent(channelList);
		_sendPayload.SetContent((byte) 0x00);

		if (_ssid != null)
			_sendPayload.SetContent(_ssid);
		_sendPayload.SetContent((byte) 0x00);

		_sendFrame.SetCommandID(CommandID.CMD_ID_WIFI);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return WIFICode.WIFI_NORESPONSE;
		}

		return WIFICode.parse(_safePayload.GetData()[2]);
	}

	// SNIC API

	public InitializationResponse SNIC_Initialization() {
		return SNIC_Initialization(0);
	}

	public InitializationResponse SNIC_Initialization(int receiveBufferSize) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_INIT_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent((byte) (receiveBufferSize >> 8));
		_sendPayload.SetContent((byte) receiveBufferSize);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new InitializationResponse(_safePayload);
	}

	public SNICCode SNIC_Cleanup() {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_CLEANUP_REQ);
		_sendPayload.SetFrameID(frameID++);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return SNICCode.SNIC_NORESPONSE;
		}

		return SNICCode.parse(_safePayload.GetData()[2]);
	}

	// / <summary>
	// / In TCP server case, Socket is the socket number returned by
	// SNIC_TCP_CLIENT_SOCKET_IND. In TCP client case, Socket can be either from
	// SNIC_CONNECT_TO_TCP_SERVER_RSP, or from the
	// SNIC_TCP_CONNECTION_STATUS_IND with SNIC_CONNECTION_UP status. In UDP
	// case, Socket is the socket number returned by SNIC_UDP_CREATE_SOCKET_REQ
	// and it must be in connected mode.
	// / A success response of this command does not guarantee the receiver
	// receives the packet. If error occurs, a SNIC_TCP_CONNECTION_STATUS_IND
	// with SNIC_SOCKET_CLOSED will be sent to the application in TCP case. No
	// indication will be sent in UDP case.
	// / Option is the action module will perform to the socket after the send
	// operation. Use it when application is sure to close or shutdown the
	// connection after sending. The effect is the same as using
	// SNIC_CLOSE_SOCKET_REQ, but round-trip UART traffic is reduced.
	// / </summary>
	// / <param name="SocketID"></param>
	// / <param name="option"></param>
	// / <returns></returns>
	public SendFromSocketResponse SNIC_SendFromSocket(byte SocketID,
			SocketSentOption option, byte[] payload, int offset, int length) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_SEND_FROM_SOCKET_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(SocketID);
		_sendPayload.SetContent(option.getValue());
		_sendPayload.SetContent((byte) (length >> 8));
		_sendPayload.SetContent((byte) length);
		_sendPayload.SetContent(payload, offset, length);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new SendFromSocketResponse(_safePayload);
	}

	public SendFromSocketResponse SNIC_SendFromSocket(byte SocketID,
			SocketSentOption option, byte[] payload) {
		return SNIC_SendFromSocket(SocketID, option, payload, 0, payload.length);
	}

	public SNICCode SNIC_SloseSocket(byte SocketID) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_CLOSE_SOCKET_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(SocketID);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return SNICCode.SNIC_NORESPONSE;
		}

		return SNICCode.parse(_safePayload.GetData()[2]);
	}

	public DHCPInfoResponse SNIC_GetDHCPInfo(WIFIInterface wifiInterface) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_GET_DHCP_INFO_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(wifiInterface.getValue());

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new DHCPInfoResponse(_safePayload);
	}

	public IPAddress SNIC_ResolveHostName(String host) {
		byte[] name = host.getBytes();

		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_RESOLVE_NAME_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(WIFIInterface.STA.getValue());
		_sendPayload.SetContent((byte) name.length);
		_sendPayload.SetContent(name);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		if (SNICCode.parse(_safePayload.GetData()[2]) == SNICCode.SNIC_SUCCESS)
			return new IPAddress(_safePayload.GetData(), 3);

		return null;
	}

	public SNICCode SNIC_ConfigureDHCPorStaticIP(DHCPConfig config) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_IP_CONFIG_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(config.GetInterface().getValue());
		_sendPayload.SetContent(config.GetDHCPMode().getValue());

		if (config.GetDHCPMode() != DHCPMode.dynamic_IP) {
			_sendPayload.SetContent(config.GetLocalIP().GetValue());
			_sendPayload.SetContent(config.GetNetmask().GetValue());
			_sendPayload.SetContent(config.GetGatewayIP().GetValue());
		}

		if (config.GetDHCPMode() == DHCPMode.soft_AP) {
			_sendPayload.SetContent(config.GetIPRangeFirst().GetValue());
			_sendPayload.SetContent(config.GetIPRangeLast().GetValue());
		}

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return SNICCode.SNIC_NORESPONSE;
		}

		return SNICCode.parse(_safePayload.GetData()[2]);
	}

	public SocketStartReceiveResponse SNIC_ConnectTCPServer(byte SocketID,
			IPAddress remoteIP, int remotePort, byte timeout) {
		return SNIC_ConnectTCPServer(SocketID, remoteIP, remotePort, timeout, 0);
	}

	// / <summary>
	// / If the connect attempt is immediately completed, the response will
	// contain SNIC_SUCCESS status, with the actual Receive buffer size.
	// / If the connect attempt is not immediately completed, the response will
	// have the SNIC_COMMAND_PENDING status. The Timeout value is the time (in
	// seconds) the module will wait before aborting the connection attempt. If
	// timeout occurs, the SNIC_TCP_CONNECTION_STATUS_IND indication with
	// SNIC_TIMEOUT status will be sent to the application. If connection is
	// successful before timeout, the SNIC_TCP_CONNECTION_STATUS_IND with
	// SNIC_CONNECTION_UP status will be sent to the application. Timeout value
	// should be non-zero.
	// / </summary>
	// / <param name="remoteHost"></param>
	// / <param name="port"></param>
	// / <param name="timeout">in seconds</param>
	// / <param name="receiveBufferSize">Receive buffer size is the maximum
	// packet size the application wants to receive per transmission. It must be
	// less than or equal to the Default receive buffer size from SNIC_INIT_REQ
	// in the module. If it is 0 or exceeds the system capability, the Default
	// receive buffer size is returned.</param>
	public SocketStartReceiveResponse SNIC_ConnectTCPServer(byte SocketID,
			IPAddress remoteIP, int remotePort, byte timeout,
			int receiveBufferSize) {
		_sendPayload.Rewind();
		_sendPayload
				.SetSubCommandID(SubCommandID.SNIC_TCP_CONNECT_TO_SERVER_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(SocketID);
		_sendPayload.SetContent(remoteIP.GetValue());
		_sendPayload.SetContent((byte) (remotePort >> 8));
		_sendPayload.SetContent((byte) remotePort);
		_sendPayload.SetContent((byte) (receiveBufferSize >> 8));
		_sendPayload.SetContent((byte) receiveBufferSize);
		_sendPayload.SetContent(timeout);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new SocketStartReceiveResponse(_safePayload);
	}

	public CreateSocketResponse SNIC_CreateTCPSocket(boolean bind) {
		return SNIC_CreateSocket(SubCommandID.SNIC_TCP_CREATE_SOCKET_REQ, bind,
				null, 0);
	}

	// / <summary>
	// / If Bind option is 0, the socket will not be bound, and Local IP address
	// and Local port should not be present. Otherwise, it will be bound to
	// Local IP address and Local port specified. 0x0 for IP or port are valid,
	// which means system assigned. Port number 5000 is reserved for internal
	// use.
	// / </summary>
	// / <param name="bing"></param>
	// / <param name="localIP"></param>
	// / <param name="port"></param>
	// / <returns></returns>
	public CreateSocketResponse SNIC_CreateTCPSocket(boolean bind,
			IPAddress localIP, int localPort) {
		return SNIC_CreateSocket(SubCommandID.SNIC_TCP_CREATE_SOCKET_REQ, bind,
				localIP, localPort);
	}

	public CreateSocketResponse SNIC_CreateUDPSocket(boolean bind) {
		return SNIC_CreateSocket(SubCommandID.SNIC_UDP_CREATE_SOCKET_REQ, bind,
				null, 0);
	}

	// / <summary>
	// / If Bind option is 0, the socket will not be bound, and Local IP address
	// and Local port should not be present. Otherwise, it will be bound to
	// Local IP address and Local port specified. 0x0 for IP or port are valid,
	// which means system assigned. Port number 5000 is reserved for internal
	// use.
	// / </summary>
	// / <param name="bind"></param>
	// / <param name="localIP"></param>
	// / <param name="port"></param>
	// / <returns></returns>
	public CreateSocketResponse SNIC_CreateUDPSocket(boolean bind,
			IPAddress localIP, int localPort) {
		return SNIC_CreateSocket(SubCommandID.SNIC_UDP_CREATE_SOCKET_REQ, bind,
				localIP, localPort);
	}

	private CreateSocketResponse SNIC_CreateSocket(SubCommandID subID,
			boolean bind, IPAddress localIP, int localPort) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(subID);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent((byte) (bind ? 0x01 : 0x00));

		if (bind) {
			if (localIP != null)
				_sendPayload.SetContent(localIP.GetValue());
			else
				_sendPayload.SetContent(IPAddress.ANY.GetValue());

			_sendPayload.SetContent((byte) (localPort >> 8));
			_sendPayload.SetContent((byte) localPort);
		}

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new CreateSocketResponse(_safePayload);
	}

	public SocketStartReceiveResponse SNIC_StartUDPReceive(byte SocketID) {
		return SNIC_StartUDPReceive(SocketID, 0);
	}

	// / <summary>
	// / The Socket should have been created by command
	// SNIC_UDP_CREATE_SOCKET_REQ. The same socket can be used in
	// SNIC_UDP_SEND_FROM_SOCKET_REQ command, so that send and receive can be
	// done via the same socket (port). The application is responsible to close
	// the socket using SNIC_CLOSE_SOCKET_REQ.
	// / Receive buffer size is the maximum packet size the application wants to
	// receive per transmission. It must be less than or equal to the Default
	// receive buffer size from SNIC_INIT_REQ in the module. If 0 or exceeds the
	// system capability, the Default receive buffer size will be used and
	// returned in the response.
	// / After this command, the Socket can receive any UDP sender with
	// connected mode or non-connected mode. The module will generate
	// SNIC_UDP_RECV_IND indication for incoming data, which includes sender’s
	// IP and port info.
	// / But if this Socket is later connected to a peer UDP server by
	// SNIC_UDP_SEND_FROM_SOCKET_REQ with Connection mode set to1, the module
	// will generate SNIC_CONNECTION_RECV_IND indication without the sender’s IP
	// and port info. See Section 5.19. After that, this Socket will only be
	// able to receive from the one sender it connects to.
	// / </summary>
	// / <param name="SocketID"></param>
	// / <param name="receiveBufferSize"></param>
	public SocketStartReceiveResponse SNIC_StartUDPReceive(byte SocketID,
			int receiveBufferSize) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_UDP_START_RECV_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(SocketID);
		_sendPayload.SetContent((byte) (receiveBufferSize >> 8));
		_sendPayload.SetContent((byte) receiveBufferSize);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new SocketStartReceiveResponse(_safePayload);
	}

	// / <summary>
	// / A socket will be created for sending the packet out through the default
	// network connection, but will be closed after the transmission. This
	// command can be used when the application just wants to send out one
	// packet to peer, and it also does not expect to receive any packets from
	// peer.
	// / </summary>
	// / <param name="remoteIP"></param>
	// / <param name="remotePort"></param>
	// / <param name="payload"></param>
	// / <param name="offset"></param>
	// / <param name="length"></param>
	public SendFromSocketResponse SNIC_SendUDPPacket(IPAddress remoteIP,
			int remotePort, byte[] payload, int offset, int length) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_UDP_SIMPLE_SEND_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(remoteIP.GetValue());
		_sendPayload.SetContent((byte) (remotePort >> 8));
		_sendPayload.SetContent((byte) remotePort);
		_sendPayload.SetContent((byte) (length >> 8));
		_sendPayload.SetContent((byte) length);
		_sendPayload.SetContent(payload, offset, length);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new SendFromSocketResponse(_safePayload);
	}

	// / <summary>
	// / A socket will be created for sending the packet out through the default
	// network connection, but will be closed after the transmission. This
	// command can be used when the application just wants to send out one
	// packet to peer, and it also does not expect to receive any packets from
	// peer.
	// / </summary>
	// / <param name="remoteIP"></param>
	// / <param name="remotePort"></param>
	// / <param name="payload"></param>
	public SendFromSocketResponse SNIC_SendUDPPacket(IPAddress remoteIP,
			int remotePort, byte[] payload) {
		return SNIC_SendUDPPacket(remoteIP, remotePort, payload, 0,
				payload.length);
	}

	// / <summary>
	// / The Socket should have been created by command
	// SNIC_UDP_CREATE_SOCKET_REQ. If SNIC_UDP_START_RECV_REQ is not called on
	// the socket, the application can only send out UDP packet from this
	// socket. If SNIC_UDP_START_RECV_REQ has been called for this socket, the
	// application can send and receive UDP packets from the socket. This
	// implies the application can send and receive packets from the same local
	// port. The application is responsible to close the socket using
	// SNIC_CLOSE_SOCKET_REQ.
	// / If Connection mode is 1, the module will first connect to the UDP
	// server then send data. Since the socket is still connected after the
	// call, application can send subsequent data using another command
	// SNIC_SEND_FROM_SOCKET_REQ.
	// / The benefit of the connected mode is that subsequent send can use
	// SNIC_SEND_FROM_SOCKET_REQ, which does not require the receiver’s IP and
	// port every time, and thus reduces overhead. If this socket is also used
	// to receive by calling SNIC_UDP_START_RECV_REQ, the receive indication to
	// the host will also omits the sender IP and port info, further reducing
	// overhead.
	// / </summary>
	// / <param name="remoteIP"></param>
	// / <param name="remotePort"></param>
	// / <param name="SocketID"></param>
	// / <param name="connectServer"></param>
	// / <param name="payload"></param>
	// / <param name="offset"></param>
	// / <param name="length"></param>
	// / <returns></returns>
	public SendFromSocketResponse SNIC_SendUDPFromSocket(IPAddress remoteIP,
			int remotePort, byte SocketID, boolean connectServer,
			byte[] payload, int offset, int length) {
		_sendPayload.Rewind();
		_sendPayload
				.SetSubCommandID(SubCommandID.SNIC_UDP_SEND_FROM_SOCKET_REQ);
		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent(remoteIP.GetValue());
		_sendPayload.SetContent((byte) (remotePort >> 8));
		_sendPayload.SetContent((byte) remotePort);
		_sendPayload.SetContent(SocketID);
		_sendPayload.SetContent((byte) (connectServer ? 0x01 : 0x00));
		_sendPayload.SetContent((byte) (length >> 8));
		_sendPayload.SetContent((byte) length);
		_sendPayload.SetContent(payload, offset, length);

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new SendFromSocketResponse(_safePayload);
	}

	// / <summary>
	// / The Socket should have been created by command
	// SNIC_UDP_CREATE_SOCKET_REQ. If SNIC_UDP_START_RECV_REQ is not called on
	// the socket, the application can only send out UDP packet from this
	// socket. If SNIC_UDP_START_RECV_REQ has been called for this socket, the
	// application can send and receive UDP packets from the socket. This
	// implies the application can send and receive packets from the same local
	// port. The application is responsible to close the socket using
	// SNIC_CLOSE_SOCKET_REQ.
	// / If Connection mode is 1, the module will first connect to the UDP
	// server then send data. Since the socket is still connected after the
	// call, application can send subsequent data using another command
	// SNIC_SEND_FROM_SOCKET_REQ.
	// / The benefit of the connected mode is that subsequent send can use
	// SNIC_SEND_FROM_SOCKET_REQ, which does not require the receiver’s IP and
	// port every time, and thus reduces overhead. If this socket is also used
	// to receive by calling SNIC_UDP_START_RECV_REQ, the receive indication to
	// the host will also omits the sender IP and port info, further reducing
	// overhead.
	// / </summary>
	// / <param name="remoteIP"></param>
	// / <param name="remotePort"></param>
	// / <param name="SocketID"></param>
	// / <param name="connectServer"></param>
	// / <param name="payload"></param>
	// / <param name="offset"></param>
	// / <param name="length"></param>
	// / <returns></returns>
	public SendFromSocketResponse SNIC_SendUDPFromSocket(IPAddress remoteIP,
			int remotePort, byte SocketID, boolean connectServer, byte[] payload) {
		return SNIC_SendUDPFromSocket(remoteIP, remotePort, SocketID,
				connectServer, payload, 0, payload.length);
	}

	public HTTPResponse SNIC_SendHTTPRequest(HTTPContent content,
			boolean isHTTPS) {
		return SNIC_SendHTTPRequest(content, isHTTPS, false);
	}

	public HTTPResponse SNIC_SendHTTPRequest(HTTPContent content,
			boolean isHTTPS, boolean chunked) {
		_sendPayload.Rewind();
		if (isHTTPS)
			_sendPayload.SetSubCommandID(SubCommandID.SNIC_HTTPS_REQ);
		else
			_sendPayload.SetSubCommandID(SubCommandID.SNIC_HTTP_REQ);

		_sendPayload.SetFrameID(frameID++);
		_sendPayload.SetContent((byte) (content.GetRemotePort() >> 8));
		_sendPayload.SetContent((byte) content.GetRemotePort());
		_sendPayload.SetContent(content.GetMethod().getValue());
		_sendPayload.SetContent(content.GetTimeout());

		_sendPayload.SetContent(content.GetRemoteHost().getBytes());
		_sendPayload.SetContent((byte) 0x00);

		_sendPayload.SetContent(content.GetURI().getBytes());
		_sendPayload.SetContent((byte) 0x00);

		_sendPayload.SetContent(content.GetContentType().getBytes());
		_sendPayload.SetContent((byte) 0x00);

		_sendPayload.SetContent(content.GetAllOtherHeaders().getBytes());
		_sendPayload.SetContent((byte) 0x00);

		if (content.GetMethod() == HTTPMethod.POST) {
			byte msb = (byte) (content.GetContentLength() >> 8);
			if (chunked)
				msb |= 0x80;
			else
				msb &= 0x7F;

			_sendPayload.SetContent(msb);
			_sendPayload.SetContent((byte) content.GetContentLength());
			_sendPayload.SetContent(content.GetBody());
		}

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(true);

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(content.GetTimeout() * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new HTTPResponse(_safePayload);
	}

	// / <summary>
	// / This command instructs the module to send a subsequent HTTP request
	// packet to the remote HTTP server if the initial SNIC_HTTP_REQ cannot
	// finish the packet due to size or other consideration. It is used when the
	// send method is POST.
	// / </summary>
	// / <param name="content"></param>
	// / <param name="chunked"></param>
	// / <returns></returns>
	public HTTPResponse SNIC_SendHTTPMoreRequest(HTTPContent content,
			boolean chunked) {
		_sendPayload.Rewind();
		_sendPayload.SetSubCommandID(SubCommandID.SNIC_HTTP_MORE_REQ);
		_sendPayload.SetFrameID(frameID++);

		byte msb = (byte) (content.GetContentLength() >> 8);
		if (chunked)
			msb |= 0x80;
		else
			msb &= 0x7F;

		_sendPayload.SetContent(msb);
		_sendPayload.SetContent((byte) content.GetContentLength());
		_sendPayload.SetContent(content.GetBody());

		_sendFrame.SetCommandID(CommandID.CMD_ID_SNIC);
		_sendFrame.SetPayload(_sendPayload);

		this.Send(!chunked);

		if (chunked)
			return null;

		synchronized (MuRata.this) {
			try {
				MuRata.this.wait(DEFAULT_WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.isSignal) {
			this.isSignal = false;
			return null;
		}

		return new HTTPResponse(_safePayload);
	}

	public CreateSocketResponse SNIC_CreateAdvancedTLSTCP(boolean bind) {
		return SNIC_CreateSocket(
				SubCommandID.SNIC_TCP_CREATE_ADV_TLS_SOCKET_REQ, bind, null, 0);
	}

	public CreateSocketResponse SNIC_CreateSimpleTLSTCP(boolean bind) {
		return SNIC_CreateSocket(
				SubCommandID.SNIC_TCP_CREAET_SIMPLE_TLS_SOCKET_REQ, bind, null,
				0);
	}

	public CreateSocketResponse SNIC_CreateAdvancedTLSTCP(boolean bind,
			IPAddress localIP, int localPort) {
		return SNIC_CreateSocket(
				SubCommandID.SNIC_TCP_CREATE_ADV_TLS_SOCKET_REQ, bind, localIP,
				localPort);
	}

	public CreateSocketResponse SNIC_CreateSimpleTLSTCP(boolean bind,
			IPAddress localIP, int localPort) {
		return SNIC_CreateSocket(
				SubCommandID.SNIC_TCP_CREAET_SIMPLE_TLS_SOCKET_REQ, bind,
				localIP, localPort);
	}

}