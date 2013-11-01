package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import util.Config;

public class ClientReader {
	private Config config; 
	
	public ClientReader(Config config) {
		this.config = config;
	}
	public String getDownloadDir() {
		return config.getString("download.dir");
	}
	
	public InetAddress getProxyHost() {
		try {
			return InetAddress.getByName(config.getString("proxy.host"));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	public int getProxyTCPPort() {
		return config.getInt("proxy.tcp.port");
	}

}
