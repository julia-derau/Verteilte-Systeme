package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import cli.Shell;
import util.ComponentFactory;
import util.Config;

/**
 * @author juliadaurer
 *
 * Main Class for FileServer
 *
 */
public class Fileserver {

	public static void main(String[] args) throws Exception {
		ComponentFactory fac = new ComponentFactory();
		Shell shell = new Shell("fileServer", new DataOutputStream(System.out), new DataInputStream(System.in));
		Config config;
		
		if(args.length == 1) {
			config = new Config("fs"+args[0]);
		} else {
			config = new Config("fs1");
		}
		
		// Start FileServer
		fac.startFileServer(config, shell);
		
	}


}