package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class ISAliveSender implements Closeable {
	private DatagramSocket dSocket;
	private FileServerReader reader;
	private Timer timer;
	private TimerTask task;
	
	public ISAliveSender(FileServerReader reader) {
		try {
			this.dSocket = new DatagramSocket();
			this.reader = reader;
		} catch (SocketException e) {
			System.err.println(e);
		}
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				String message = "!alive " + ISAliveSender.this.reader.getTCPPort();
				try {
					ISAliveSender.this.dSocket.send(new DatagramPacket(message.getBytes(), message.getBytes().length, ISAliveSender.this.reader.getProxyHost(), ISAliveSender.this.reader.getProxyUDPPort()));
				} catch (IOException e) {
					System.err.println(e);
				}
				
			}
		};
		timer.schedule(task,0,reader.getFileServerAlive());
		
	}

	@Override
	public void close() throws IOException {
		this.task.cancel();
		this.timer.cancel();
	}

}
