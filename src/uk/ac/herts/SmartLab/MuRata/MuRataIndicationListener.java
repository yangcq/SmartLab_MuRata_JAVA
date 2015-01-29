package uk.ac.herts.SmartLab.MuRata;

import uk.ac.herts.SmartLab.MuRata.Indication.*;

public interface MuRataIndicationListener {

	public void onPowerUpIndication(PowerUpIndication indication);

	public void onScanResultIndication(SSIDRecordIndication indication);

	public void onTcpConnectionStatusIndication(TCPStatusIndication indication);

	public void onSocketReceiveIndication(SocketReceiveInidcation indication);

	public void onUDPReceiveIndication(UDPReceivedIndication indication);

	public void onWIFIConnectionIndication(WIFIConnectionIndication indication);

	public void onHTTPResponseIndication(HTTPResponseIndication indication);
}
