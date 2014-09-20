package proxy;

import java.io.IOException;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import cli.Command;
import cli.Shell;

public class ProxyCli implements IProxyCli {
	private ProxyReader reader;
	private UserReader userReader;
	private Shell shell;
	private ExecutorService pool = Executors.newCachedThreadPool();
	private Set<FileServerInfo> fileserver = new HashSet<FileServerInfo>();
	private Set<UserInfo> userList;
	private FileServerListener fileServerListener;
	private ClientListener clientListener;
	private Config config;

	public ProxyCli(ProxyReader reader, Shell shell) throws IOException {
		this.reader = reader;
		this.shell = shell;

		this.config = new Config("user");
		this.userReader = new UserReader(config);
		this.userList = new HashSet<UserInfo>(userReader.getUsers());

		this.fileServerListener = new FileServerListener(fileserver, this.reader.getFileserverPeriod(), this.reader.getTCP(), this.reader.getUDP(), this.reader.getFileserverTimeout());
		this.clientListener = new ClientListener(new ConnectionFactory() {
			@Override
			public Proxy create(final Socket s) {
				try {
					return new Proxy(ProxyCli.this.reader, ProxyCli.this.userList, ProxyCli.this.fileserver, s);
				} catch (IOException e) {
					System.err.println("Error occured while initialzing the ClientListener in ProxyCli");
				}
				return null;
			}
		}, this.reader.getTCP());

		this.shell.register(this);
		this.pool.execute(shell);
		this.pool.execute(fileServerListener);
		this.pool.execute(clientListener);

	}


	@Override
	@Command
	public Response fileservers() {
		synchronized(this.fileserver) {
			List<model.FileServerInfo> list = new ArrayList<model.FileServerInfo>();
			for(FileServerInfo f1 : this.fileserver) {
				list.add(new model.FileServerInfo(f1.getAddress(),f1.getPort(),f1.getUsage(),f1.isOnline()));
			}
			return new FileServerInfoResponse(list);
		}
	}

	@Override
	@Command
	public Response users() {
		List<model.UserInfo> list = new ArrayList<model.UserInfo>();
		synchronized(userList) {
			for(UserInfo f1 : userList) {
				list.add(new model.UserInfo(f1.getName(), f1.getCredits(), f1.isOnline()));
			}
			return new UserInfoResponse(list);
		}
	}

	@Override
	@Command
	public MessageResponse exit() {	
		this.pool.shutdownNow();
		this.clientListener.close();
		this.fileServerListener.close();

		this.shell.close();

		return new MessageResponse("Shell is terminated");

	}
}
