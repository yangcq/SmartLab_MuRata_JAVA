package uk.ac.herts.SmartLab.MuRata.Type;

import java.util.HashMap;
import java.util.Map.Entry;

public class HTTPContent {
	private static final String HEADER_SEPARATE = ": ";
	private static final String HEADER_TEMINATE = "\r\n";

	private HTTPMethod method;
	private byte timeout;
	private int remotePort;
	private String remoteHost;
	private String uri;

	private int contentLength;
	private String contentType;

	private HashMap<String, String> otherHeaders = new HashMap<String, String>();

	private byte[] body;

	public HTTPContent(HTTPMethod method, String remoteHost, int remotePort,
			String uri, byte timeout, String contentType) {
		this.SetMethod(method).SetRemoteHost(remoteHost)
				.SetRemotePort(remotePort).SetURI(uri).SetTimeout(timeout)
				.SetContentType(contentType);
	}

	public HTTPMethod GetMethod() {
		return this.method;
	}

	public HTTPContent SetMethod(HTTPMethod method) {
		this.method = method;
		return this;
	}

	public byte GetTimeout() {
		return this.timeout;
	}

	public HTTPContent SetTimeout(byte timeout) {
		this.timeout = timeout;
		return this;
	}

	public int GetRemotePort() {
		return this.remotePort;
	}

	public HTTPContent SetRemotePort(int port) {
		this.remotePort = port;
		return this;
	}

	public String GetRemoteHost() {
		return this.remoteHost;
	}

	public HTTPContent SetRemoteHost(String host) {
		this.remoteHost = host;
		return this;
	}

	public String GetURI() {
		return this.uri;
	}

	public HTTPContent SetURI(String uri) {
		this.uri = uri;
		return this;
	}

	public String GetContentType() {
		return this.contentType;
	}

	public HTTPContent SetContentType(String contentType) {
		if (contentType == null)
			this.contentType = "";
		else
			this.contentType = contentType;
		return this;
	}

	public String GetOtherHeader(String key) {
		return (String) this.otherHeaders.get(key);
	}

	public HTTPContent SetOtherHeader(String key, String value) {
		this.otherHeaders.put(key, value);
		return this;
	}

	public String GetAllOtherHeaders() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : this.otherHeaders.entrySet()) {
			sb.append(entry.getKey());
			sb.append(HEADER_SEPARATE);
			sb.append(entry.getValue());
			sb.append(HEADER_TEMINATE);
		}
		return sb.toString();
	}

	public byte[] GetBody() {
		return this.body;
	}

	public HTTPContent SetBody(String body) {
		if (body == null) {
			this.contentLength = 0;
			this.body = null;
			return this;
		}

		return SetBody(body.getBytes());
	}

	public HTTPContent SetBody(byte[] body) {
		if (body == null)
			this.contentLength = 0;
		else
			this.contentLength = body.length;

		this.body = body;
		return this;
	}

	public HTTPContent ClearBody() {
		this.contentLength = 0;
		this.body = null;
		return this;
	}

	public int GetContentLength() {
		return this.contentLength;
	}

}