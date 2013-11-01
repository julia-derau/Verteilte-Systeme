package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hamcrest.Factory;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.MessageResponse;

public class ClientListener implements Runnable{
	private ServerSocket ssocket;
	private Socket socket;
	private ConnectionFactory fac;
	private ExecutorService service = Executors.newCachedThreadPool();
	private int tcpPort;



	public ClientListener(ConnectionFactory fac, int tcpPort) {
		this.fac = fac;
		this.tcpPort = tcpPort;
		try {
			this.ssocket = new ServerSocket(tcpPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		while(true) {
			Proxy p = null;
			try {
				socket = ssocket.accept();
				p = this.fac.create(socket);
				service.execute(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				try {
					if (p != null ) p.close();
					//ssocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


	}
}
