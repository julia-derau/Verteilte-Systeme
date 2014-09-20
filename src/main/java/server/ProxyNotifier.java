package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class ProxyNotifier implements Closeable {
	private DatagramSocket socket;
	private Timer timer;
	private DatagramPacket datagramPacket;

	public ProxyNotifier(FileServerReader reader) {
		try {
			this.socket = new DatagramSocket();
		} catch (SocketException e1) {
			System.err.println("DatagramSocket for sending UDP-Packets in ProxyNotifier is closed");
		}
		this.timer = new Timer();
		
		String message = "!alive "+Integer.toString(reader.getTCPPort());
		this.datagramPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, reader.getProxyHost(), reader.getProxyUDPPort());
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					socket.send(datagramPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		timer.schedule(task, 0, reader.getFileServerAlive());
	}



	@Override
	public void close() {	
		timer.cancel();
		
		if(!socket.isClosed()) {
			socket.close();
		}
	}

}
