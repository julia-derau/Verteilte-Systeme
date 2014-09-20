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
	private ProxyNotifier notifier;
	private ClientProxyListener listener;
	private ISAliveSender sender;


	public FileServerCli(FileServerReader reader, Shell shell) throws IOException {
		this.shell = shell;
		this.shell.register(this);

		//this.notifier = new ProxyNotifier(reader);
		this.sender = new ISAliveSender(reader);
		this.listener = new ClientProxyListener(reader);

		this.pool.execute(shell);
		this.pool.execute(listener);
	}

	@Override
	@Command
	public MessageResponse exit() {
		this.pool.shutdownNow();
		this.listener.close();
		//this.notifier.close();
		try {
			this.sender.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.shell.close();

		return new MessageResponse("Shell is terminated");
	}

}
