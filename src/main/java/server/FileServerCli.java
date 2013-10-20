package server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.response.MessageResponse;
import cli.Command;
import cli.Shell;

public class FileServerCli implements IFileServerCli {
	private ExecutorService pool = Executors.newCachedThreadPool();
	private Shell shell;
	private FileServerReader reader;
	private ProxyNotifier notifier;
	

	public FileServerCli(FileServerReader reader, Shell shell)
			throws IOException {
		this.shell = shell;
		this.reader = reader;
		shell.register(this);

		pool.execute(shell);
	
		shell.writeLine("FileServer.Dir:" + this.reader.getFileServerDir()
				+ ", TCP-Port: " + this.reader.getTCPPort() + ", Proxy-Host: "
				+ reader.getProxyHost() + ", Proxy-UDP-Port: " + this.reader.getProxyUDPPort() + ", FileServer-Alive: "+ this.reader.getFileServerAlive());
		
		this.notifier = new ProxyNotifier(this.reader.getFileServerAlive(), this.reader.getTCPPort(), this.reader.getProxyHost(), this.reader.getProxyUDPPort());

	}

	@Override
	@Command
	public MessageResponse exit() {
		this.notifier.close();
		this.pool.shutdownNow();
		this.shell.close();

		return new MessageResponse("Shell is terminated");
	}

}
