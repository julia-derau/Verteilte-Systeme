package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import proxy.FileServerInfo;
import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.Closeable;

public class FileServerListener implements Runnable {
	private DatagramSocket socket;
	private DatagramPacket datagramPacket;
	private Timer timer;
	private Set<FileServerInfo> fileserver;
	private HashMap<FileServerInfo, Long> fileServerAliveTime;
	private int timeOut;



	public FileServerListener(Set<FileServerInfo> fileserver, int checkPeriod, int tcpPort, int udpPort, int timeOut) throws SocketException {
		this.socket = new DatagramSocket(udpPort);
		this.timer = new Timer();
		this.fileserver = fileserver;
		this.fileServerAliveTime = new HashMap<FileServerInfo, Long>();
		this.timeOut = timeOut;
		String message = "!alive 9999999999";

		this.datagramPacket = new DatagramPacket(message.getBytes(), message.getBytes().length);
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				synchronized(FileServerListener.this.fileserver) {
					for(FileServerInfo f1 : FileServerListener.this.fileserver) {
						if(fileServerAliveTime.containsKey(f1)) {
							long lastTime = fileServerAliveTime.get(f1);

							if((System.currentTimeMillis()-lastTime) > FileServerListener.this.timeOut) {
								f1.setOnline(false);
								FileServerListener.this.fileServerAliveTime.remove(f1);
								System.out.println("Paket nicht alle drei Sekunden eingelangt");
							}

						}

					}	
				}
			}	
		};
		timer.schedule(task, 0, checkPeriod);

	}

	@Override
	public void run() {
		while(true) {
			try {
				this.socket.receive(datagramPacket);
				String alive = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
				Scanner sc = new Scanner(alive);
				int port = 0;
				
				if(sc.hasNext()) {
					String msg = sc.next();
					if(sc.hasNextInt()) {
						port = sc.nextInt();
						
						FileServerInfo temp = new FileServerInfo(datagramPacket.getAddress(), port,0, false);

						synchronized (fileserver) {
							if(!fileserver.contains(temp)) {
								fileserver.add(temp);
							}

							for(FileServerInfo f1 : fileserver) {
								if(f1.equals(temp)) {
									f1.setOnline(true);
									fileServerAliveTime.put(f1, System.currentTimeMillis());
									break;
								}
							}
						}
					} else {
						System.out.println("ERROOOOOOR");
					}
				} else {
					System.out.println("ERROOOOOOR");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} /*finally {
				timer.cancel();
				if(!socket.isClosed()) {
					socket.close();
				}
			}*/
		}

	}

}
