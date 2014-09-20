package server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import util.Config;

/**
 * @author juliadaurer
 * 
 * Reads the attributes of FileServer out of the Config 
 *
 */
public class FileServerReader {
	private Config config; 
	
	public FileServerReader(Config config) {
		this.config = config;
	}
	
	public String getFileServerDir() {
		return config.getString("fileserver.dir");
	}
	public InetAddress getProxyHost() {
		try {
			return InetAddress.getByName(config.getString("proxy.host"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	public int getTCPPort() {
		return config.getInt("tcp.port");
	}
	public int getProxyUDPPort() {
		return config.getInt("proxy.udp.port");
	}
	public int getFileServerAlive() {
		return config.getInt("fileserver.alive");
	}
}
