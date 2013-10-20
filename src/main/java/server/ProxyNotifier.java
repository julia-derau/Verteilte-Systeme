package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class ProxyNotifier implements Closeable {
	private DatagramSocket socket;
	private Timer timer;
	private DatagramPacket datagramPacket;

	public ProxyNotifier(long period, int tcpPort, InetAddress proxyAddress, int udpProxyPort) throws SocketException {
		// TCP Port von FileServer, TCP Port wartet auf Anfragen von Client
		// UDP Proxy Port
		this.socket = new DatagramSocket();
		this.timer = new Timer();
		String message = "!alive"+Integer.toString(tcpPort);
		this.datagramPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, proxyAddress, udpProxyPort);
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					socket.send(datagramPacket);
					System.out.println("Alive - Send :P ");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		timer.schedule(task, 0, period);
	}



	@Override
	public void close() {
		timer.cancel();
		if(!socket.isClosed()) {
			socket.close();
		}
	}

}
