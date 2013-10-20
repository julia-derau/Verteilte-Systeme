package proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.ProxyNotifier;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import model.UserInfo;
import cli.Command;
import cli.Shell;

public class ProxyCli implements IProxyCli {
	private ProxyReader reader;
	private Shell shell;
	private ExecutorService pool = Executors.newCachedThreadPool();
	private FileServerInfoResponse fileserverResponse = new FileServerInfoResponse(new ArrayList<FileServerInfo>());
	private List<UserInfo> userList = new ArrayList<>();
	private FileServerListener listener;



	public ProxyCli(ProxyReader reader, Shell shell) throws IOException {
		this.reader = reader;
		this.shell = shell;
		shell.register(this);
		pool.execute(shell);
		shell.writeLine("TCP:" + this.reader.getTCP() + ", UDP:" + reader.getUDP()
				+ ", Fileserver-Timeout:" + this.reader.getFileserverTimeout() + ", Fileserver-CheckPeriod: "+ this.reader.getFileserverPeriod());
		
		this.listener = new FileServerListener(fileserverResponse, this.reader.getFileserverTimeout(), this.reader.getTCP(), this.reader.getUDP());
		this.listener.run();
	}

	
	@Override
	@Command
	public Response fileservers() {
		return fileserverResponse;
		
	}

	@Override
	@Command
	public Response users() {
		return new UserInfoResponse(userList);
	}

	@Override
	@Command
	public MessageResponse exit() {		

		shell.close();
		pool.shutdown();
		
		return new MessageResponse("Shell is terminated");

	}
}
