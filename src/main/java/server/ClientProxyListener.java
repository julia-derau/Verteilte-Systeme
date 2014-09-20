package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientProxyListener implements Runnable, Closeable{
	private ServerSocket proxyClientSocket;
	private Socket socket;
	private ExecutorService service = Executors.newCachedThreadPool();
	private FileServerReader reader;
	private List<Closeable> actualThreads;
	private boolean close = false;

	public ClientProxyListener(FileServerReader reader) {
		this.actualThreads = Collections.synchronizedList(new ArrayList<Closeable>());

		try {
			this.reader = reader;
			this.proxyClientSocket = new ServerSocket(reader.getTCPPort());
		} catch (IOException e) {
			System.err.print("IO Exception at the ServerSocket in TCP Listener");
		}
	}

	@Override
	public void run() {
		
		while(!close) {
			try {
				this.socket = proxyClientSocket.accept();
				FileServer fs = new CloseableFS(this.reader, socket);
				this.actualThreads.add(fs);
				this.service.execute(fs);

			} catch (SocketException e) {
				break;
			} catch (IOException e) {
				System.err.println("IO Exception at the ServerSocket in TCP Listener while listening");
			} 
		}

	}

	@Override
	public void close() {
		this.service.shutdownNow();
		if(this.socket != null) {
			if(!this.socket.isClosed()) {
				try {
					this.socket.close();
				} catch (IOException e) {
					System.err.println("IO Exception at the Socket in TCP Listener while closing");
				}
			}
		}
		if(this.proxyClientSocket != null) {
			if(!this.proxyClientSocket.isClosed()) {
				this.close = true;
				try {
					this.proxyClientSocket.close();
				} catch (IOException e) {
					System.err.println("IO Exception at the ServerSocket in TCP Listener while closing");
				}
			}
		}
	}
	private class CloseableFS extends FileServer{

		public CloseableFS(FileServerReader reader, Socket socket) {
			super(ClientProxyListener.this.reader, ClientProxyListener.this.socket);
		}

		public void close() {
			try {
				super.close();
				ClientProxyListener.this.actualThreads.remove(this);
				
			} catch (IOException e) {
				System.err.println("Closing of the FileServer went wrong");
			}
		}

	}

}
