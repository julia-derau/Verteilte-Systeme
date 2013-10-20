package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import message.Response;
import message.response.FileServerInfoResponse;
import model.FileServerInfo;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.Closeable;

public class FileServerListener implements Runnable, Closeable {
	private DatagramSocket socket;
	private DatagramPacket datagramPacket;
	private DatagramPacket originalPacket;
	private ExecutorService service = Executors.newCachedThreadPool();
	private Timer timer;
	private FileServerInfoResponse fileserver;


	public FileServerListener(final FileServerInfoResponse fileserver, long checkPeriod, int tcpPort, int udpPort) throws SocketException {
		this.socket = new DatagramSocket(udpPort);
		this.timer = new Timer();
		this.fileserver = fileserver;
		String message = "!alive" + Integer.toString(tcpPort);
		
		this.originalPacket = new DatagramPacket(message.getBytes(), message.getBytes().length);		

		this.datagramPacket = new DatagramPacket(message.getBytes(), message.getBytes().length);
		service.execute(this);
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				for(FileServerInfo f1 : fileserver.getFileServerInfo()) {
					System.out.println("Paket alle drei Sekunden eingelangt");
					
					try {
						socket.receive(datagramPacket);
						// TODO FEHLER 
						System.out.println("!!!! " + originalPacket.getPort() + "!!!!!" + datagramPacket.getPort());
						if(originalPacket.getPort() == datagramPacket.getPort()) {
							System.out.println("Oh nein!:O  Kein Paket da!");
							fileserver.removeFileServer(f1);
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}	
			}	
		};
		timer.schedule(task, 0, checkPeriod);

	}

	@Override
	public void close() throws WebServiceException {
		timer.cancel();
		if(!socket.isClosed()) {
			socket.close();
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				this.socket.receive(datagramPacket);
				System.out.println("Paket bekommen :) ");
				System.out.println(new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength()));

				// TODO USAGE HERAUSFINDEN!!!
				FileServerInfo temp = new FileServerInfo(datagramPacket.getAddress(), datagramPacket.getPort(),datagramPacket.getOffset(), true);
				
				fileserver.addFileServer(temp);
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
