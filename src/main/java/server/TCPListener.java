package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.ConnectionFactory;
import proxy.Proxy;

public class TCPListener implements Runnable{
	private ServerSocket proxyClientSocket;
	private Socket socket;
	private ExecutorService service = Executors.newCachedThreadPool();
	private FileServerReader reader;

	public TCPListener(FileServerReader reader) {
		try {
			this.reader = reader;
			this.proxyClientSocket = new ServerSocket(reader.getTCPPort());
			System.out.println("pohort"+proxyClientSocket.getLocalPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true) {
			FileServer fs = null;
			try {
				socket = proxyClientSocket.accept();
				fs = new FileServer(this.reader, socket);
				service.execute(fs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/**finally {
				try {
					if (fs != null ) //fs.close();
					//ssocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}**/
		}
		
	}

}
