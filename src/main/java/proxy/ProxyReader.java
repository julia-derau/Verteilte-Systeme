package proxy;

import util.Config;

public class ProxyReader {
	private Config config; 
	
	public ProxyReader(Config config) {
		this.config = config;
	}
	public int getTCP() {
		return config.getInt("tcp.port");
	}
	public int getUDP() {
		return config.getInt("udp.port");
	}
	public int getFileserverTimeout() {
		return config.getInt("fileserver.timeout");
	}
	public int getFileserverPeriod() {
		return config.getInt("fileserver.checkPeriod");
	}

}
