package proxy;

import java.io.Serializable;

/**
 * Contains information about a user account.
 */
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1231468054964602727L;

	private String name;
	private long credits;
	private boolean online;

	public UserInfo(String name, long credits, boolean online) {
		this.name = name;
		this.credits = credits;
		this.online = online;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-7s %3$13d", name, isOnline() ? "online" : "offline", credits);
	}

	public String getName() {
		return name;
	}

	public long getCredits() {
		return credits;
	}

	public boolean isOnline() {
		return online;
	}
	public void setCredits(long credits) {
		this.credits = credits;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		UserInfo other = (UserInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
