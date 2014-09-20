package proxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import util.Config;

public class UserReader {
	private Config config; 
	private Properties properties;

	public UserReader(Config config) {
		this.config = config;
		this.properties = new Properties();
	}
	public int getCredits(String username) {
		return this.config.getInt(username+".credits");
	}
	public int getPassword(String username) {
		return this.config.getInt(username+".password");
	}
	public Set<UserInfo> getUsers() {
		try {
			this.properties.load(new FileInputStream("src/main/resources/user.properties"));
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		Set<String> set = this.properties.stringPropertyNames();
		Set<UserInfo> userSet = new HashSet<UserInfo>();
		for(String s : set) {
			String name = s.substring(0,s.indexOf("."));
			UserInfo u = new UserInfo(name, getCredits(name), false);
			if(!userSet.contains(u)) {
				userSet.add(u);
			}
		}
		return userSet;
	}
}