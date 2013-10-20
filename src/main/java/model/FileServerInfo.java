package model;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Contains information about a {@link server.IFileServer} and its state.
 */
public class FileServerInfo implements Serializable {
	private static final long serialVersionUID = 5230922478399546921L;

	private InetAddress address;
	private int port;
	private long usage;
	private boolean online;

	public FileServerInfo(InetAddress address, int port, long usage, boolean online) {
		this.address = address;
		this.port = port;
		this.usage = usage;
		this.online = online;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-5d %3$-7s %4$13d",
				getAddress().getHostAddress(), getPort(),
				isOnline() ? "online" : "offline", getUsage());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileServerInfo other = (FileServerInfo) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public long getUsage() {
		return usage;
	}

	public boolean isOnline() {
		return online;
	}
}
