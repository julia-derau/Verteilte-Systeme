package proxy;

import java.net.Socket;

public interface ConnectionFactory {
	public Proxy create(Socket s);
}
