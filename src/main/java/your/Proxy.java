package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import cli.Shell;
import util.ComponentFactory;
import util.Config;


/**
 * @author juliadaurer
 *
 * Main Class for Proxy
 *
 */
public class Proxy {

	public static void main(String[] args) throws Exception {
		ComponentFactory fac = new ComponentFactory();
		Config config = new Config("proxy");
		Shell shell = new Shell("proxy", new DataOutputStream(System.out), new DataInputStream(System.in));
		
		fac.startProxy(config, shell);
	}

}
