package server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import message.response.MessageResponse;
import cli.Command;
import cli.Shell;

public class FileServerCli implements IFileServerCli {
	private ExecutorService pool = Executors.newCachedThreadPool();
	private Shell shell;
	private FileServerReader reader;
	private ProxyNotifier notifier;
	private TCPListener listener;
	

	public FileServerCli(FileServerReader reader, Shell shell) throws IOException {
		this.shell = shell;
		this.reader = reader;
		shell.register(this);

		pool.execute(shell);
		this.notifier = new ProxyNotifier(this.reader.getFileServerAlive(), this.reader.getTCPPort(), this.reader.getProxyHost(), this.reader.getProxyUDPPort());
		this.listener = new TCPListener(reader);
		pool.execute(listener);
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
