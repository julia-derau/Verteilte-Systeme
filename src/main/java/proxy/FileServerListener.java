package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import proxy.FileServerInfo;


public class FileServerListener implements Runnable, Closeable {
	private DatagramSocket socket;
	private DatagramPacket datagramPacket;
	private Timer timer;
	private Set<FileServerInfo> fileserver;
	private HashMap<FileServerInfo, Long> fileServerAliveTime;
	private int timeOut;
	private boolean close = false;

	public FileServerListener(Set<FileServerInfo> fileserver, int checkPeriod, int tcpPort, int udpPort, int timeOut) {
		try {
			this.socket = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			System.err.print("Exception at the instantiation of the DatagramSocket in FileServerListener");
		}
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
						if(FileServerListener.this.fileServerAliveTime.containsKey(f1)) {
							long lastTime = fileServerAliveTime.get(f1);

							if((System.currentTimeMillis()-lastTime) > FileServerListener.this.timeOut) {
								f1.setOnline(false);
								FileServerListener.this.fileServerAliveTime.remove(f1);
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
		while(!close) {
			Scanner sc = null;
			try {
				this.socket.receive(datagramPacket);
				String alive = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
				sc =  new Scanner(alive);
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
						System.err.println("Error occured, while reading with the scanner in FileserverListener");
					}
				} else {
					System.err.println("Error occured, while reading with the scanner in FileserverListener");
				}

			} catch (SocketException e) {
				return;
			} catch (IOException e) {
				System.err.println("Error occured while waiting for incoming messages from the FileServer in FileserverListener");
			} finally {
				if(sc != null) {
					sc.close();
				}
			}
		}

	}

	@Override
	public void close() {
		this.timer.cancel();

		if(socket != null) {
			if(!socket.isClosed()) {
				this.close = true;
				this.socket.close();
			}
		}
	}
}
