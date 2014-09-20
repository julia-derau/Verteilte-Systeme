package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientListener implements Runnable, Closeable {
	private ServerSocket clientSocket;
	private Socket socket;
	private ConnectionFactory fac;
	private ExecutorService service = Executors.newCachedThreadPool();
	private List<Closeable> actualThreads;
	private boolean close = false;

	public ClientListener(ConnectionFactory fac, int tcpPort) {
		this.actualThreads = new ArrayList<Closeable>();
		this.fac = fac;
		try {
			this.clientSocket = new ServerSocket(tcpPort);
		} catch (IOException e) {
			System.err.print("IO Exception at the ServerSocket in Client Listener");
		}
	}
	@Override
	public void run() {
		while(!close) {
			Proxy p = null;
			try {
				this.socket = this.clientSocket.accept();
				p = this.fac.create(this.socket);
				this.actualThreads.add(p);
				this.service.execute(p);
			} catch (SocketException e) {
				break;
			} catch (IOException e) {
				System.err.println("IO Exception at the ServerSocket in Client Listener while listening");
			} 
		}
	}
	@Override
	public void close() {
		this.service.shutdownNow();
		for(Closeable c : this.actualThreads) {
			try {
				c.close();
			} catch (IOException e) {
				System.err.println("IO Exception while closind the Proxy-Threads in Client Listener");
			}
		}
		if(this.socket != null) {
			if(!this.socket.isClosed()) {
				try {
					this.socket.close();
				} catch (IOException e) {
					System.err.println("IO Exception at the Socket in Client Listener while closing");
				}
			}
		}
		if(this.clientSocket != null) {
			if(!this.clientSocket.isClosed()) {
				this.close = true;
				try {
					this.clientSocket.close();
				} catch (IOException e) {
					System.err.println("IO Exception at the ServerSocket in Client Listener while closing");
				}
			}
		}	
	}
}
