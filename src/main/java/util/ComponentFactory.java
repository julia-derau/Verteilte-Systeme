package util;

import cli.Shell;
import client.ClientCli;
import client.ClientReader;
import client.IClientCli;
import proxy.IProxyCli;
import proxy.ProxyCli;
import proxy.ProxyReader;
import server.FileServerCli;
import server.FileServerReader;
import server.IFileServerCli;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory {

	/**
	 * Creates and starts a new client instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IClientCli startClient(Config config, Shell shell) throws Exception {
		ClientReader reader = new ClientReader(config);
		
		IClientCli client = new ClientCli(reader, shell);
		return client;
	}

	/**
	 * Creates and starts a new proxy instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IProxyCli startProxy(Config config, Shell shell) throws Exception {	
		ProxyReader reader = new ProxyReader(config);
		
		IProxyCli proxy = new ProxyCli(reader, shell);
		return proxy;
	}

	/**
	 * Creates and starts a new file server instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IFileServerCli startFileServer(Config config, Shell shell) throws Exception {
		FileServerReader reader = new FileServerReader(config);
		
		IFileServerCli file = new FileServerCli(reader, shell);
		return file;
	}
}
