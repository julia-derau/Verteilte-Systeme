package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import cli.Shell;
import util.ComponentFactory;
import util.Config;

public class Fileserver {

	public static void main(String[] args) throws Exception {
		ComponentFactory fac = new ComponentFactory();
		Config config;
		if(args.length == 1) {
			config = new Config(args[0]);
		} else {
			config = new Config("fs1");
		}
		Shell shell = new Shell("FileServer", new DataOutputStream(System.out), new DataInputStream(System.in));
		fac.startFileServer(config, shell);
		
	}


}