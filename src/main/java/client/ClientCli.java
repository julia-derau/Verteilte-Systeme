package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.FileServerInfo;
import cli.Command;
import cli.Shell;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.CreditsResponse;
import message.response.DownloadFileResponse;
import message.response.DownloadTicketResponse;
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;

public class ClientCli implements IClientCli {
	private ClientReader reader;
	private Shell shell;
	private ExecutorService pool = Executors.newCachedThreadPool();
	private Socket socket; 
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public ClientCli(ClientReader reader, Shell shell) throws IOException {

		this.reader = reader;
		this.shell = shell;
		this.shell.register(this);
		this.socket = new Socket(reader.getProxyHost(), reader.getProxyTCPPort());
		this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
		this.inputStream = new ObjectInputStream(this.socket.getInputStream());
		this.pool.execute(shell);	

	}

	@Override
	@Command
	public LoginResponse login(String username, String password) throws IOException {
		Request request = new LoginRequest(username, password);
		this.outputStream.writeObject(request);

		try {
			Object o =  this.inputStream.readObject();
			return (LoginResponse) o;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	@Command
	public Response credits() throws IOException {
		Request request = new CreditsRequest();
		this.outputStream.writeObject(request);
		try {
			return (CreditsResponse) this.inputStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("Creditsrequest was not successful");
	}

	@Override
	@Command
	public Response buy(long credits) throws IOException {
		Request request = new BuyRequest(credits);
		this.outputStream.writeObject(request);

		try {
			return (Response) this.inputStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("BuyRequest was not successful");

	}


	@Override
	@Command
	public Response list() throws IOException {
		Request request = new ListRequest();
		this.outputStream.writeObject(request);

		try {
			return (Response) this.inputStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("ListRequest was not successful");
	}

	@Override
	@Command
	public Response download(String filename) throws IOException {
		Request request = new DownloadTicketRequest(filename);
		this.outputStream.writeObject(request);

		try {
			DownloadTicketResponse downloadTicketResponse = (DownloadTicketResponse) this.inputStream.readObject();
			Socket s = null;
			ObjectOutputStream outputStream = null;
			ObjectInputStream inputStream = null;

			try {
				s = new Socket(downloadTicketResponse.getTicket().getAddress(), downloadTicketResponse.getTicket().getPort());
				System.out.println(s.getPort());
				outputStream = new ObjectOutputStream(s.getOutputStream());
				inputStream = new ObjectInputStream(s.getInputStream());

				outputStream.writeObject(new DownloadFileRequest(downloadTicketResponse.getTicket()));

				Object o = inputStream.readObject();
				DownloadFileResponse downloadFile = (DownloadFileResponse) o;
				
				File dir = new File(reader.getDownloadDir());

				File temp = new File(dir, filename);
				FileOutputStream fileInputStream = new FileOutputStream(temp);

				fileInputStream.write(downloadFile.getContent());
				fileInputStream.close();
				return new MessageResponse("File successfully downloaded.");

			} catch (IOException | ClassNotFoundException ex1) {
				ex1.printStackTrace();
				return new MessageResponse("Irgendwos hot ned passt :("
						+ ex1.getMessage());
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("DownloadRequest was not successful");
	}

	@Override
	@Command
	public MessageResponse upload(String filename) throws IOException {
		File f = new File(reader.getDownloadDir());
		File[] fileArray = f.listFiles();
		File temp = null;
		for(int i = 0; i < fileArray.length; i++) {
			if(fileArray[i].getName().equals(filename)) {
				temp = fileArray[i];
			}
		}
		FileInputStream fileInputStream = new FileInputStream(temp);
	    byte[] data = new byte[(int) temp.length()];
	    fileInputStream.read(data);
	    fileInputStream.close();
		Request request = new UploadRequest(filename, 1, data);
		this.outputStream.writeObject(request);

		try {
			return (MessageResponse) this.inputStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("DownloadRequest was not successful");
	}

	@Override
	@Command
	public MessageResponse logout() throws IOException {
		Request request = new LogoutRequest();
		this.outputStream.writeObject(request);

		try {
			return (MessageResponse) this.inputStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("DownloadRequest was not successful");
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		this.outputStream.close();
		this.inputStream.close();
		this.pool.shutdownNow();
		this.socket.close();
		this.shell.close();
		return new MessageResponse("Client is terminated");

	}
}
