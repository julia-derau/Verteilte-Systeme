package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import cli.Shell;
import util.ComponentFactory;
import util.Config;

/**
 * @author juliadaurer
 *
 * Main Class for Client
 *
 */
public class Client {
	public static void main(String[] args) {
		ComponentFactory factory = new ComponentFactory();
		Config config = new Config("client");
		Shell shell = new Shell("client", new DataOutputStream(System.out), new DataInputStream(System.in));
		
		try {
			factory.startClient(config, shell);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
