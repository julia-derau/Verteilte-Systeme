package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	private boolean login = false;

	public ClientCli(ClientReader reader, Shell shell) throws IOException {
		this.reader = reader;
		this.shell = shell;
		this.shell.register(this);
		this.pool.execute(shell);		
		
		this.socket = new Socket(reader.getProxyHost(), reader.getProxyTCPPort());
		this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
		this.inputStream = new ObjectInputStream(this.socket.getInputStream());

	}

	@Override
	@Command
	public LoginResponse login(String username, String password) throws IOException {
		Request request = new LoginRequest(username, password);
		this.outputStream.writeObject(request);

		try {
			Object o =  this.inputStream.readObject();
			LoginResponse response =  (LoginResponse) o;
			if(response.getType().equals(Type.SUCCESS)) {
				login = true;
			}
			return response;
		} catch (ClassNotFoundException e) {
			System.err.println("Error occured during the LogIn Process in ClientCli");
		}
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	@Command
	public Response credits() throws IOException {
		if(login) {
			Request request = new CreditsRequest();
			this.outputStream.writeObject(request);
			try {
				return (Response) this.inputStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Error occured during the Credit Request Process in ClientCli");
			}
			return new MessageResponse("Creditsrequest was not successful");
		}
		return new MessageResponse("You are not logged in");

	}

	@Override
	@Command
	public Response buy(long credits) throws IOException {
		if(login) {
			Request request = new BuyRequest(credits);
			this.outputStream.writeObject(request);

			try {
				return (Response) this.inputStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Error occured during the Buy Process in ClientCli");
			}
			return new MessageResponse("BuyRequest was not successful");
		}
		return new MessageResponse("You are not logged in");

	}


	@Override
	@Command
	public Response list() throws IOException {
		if(login) {
			Request request = new ListRequest();
			this.outputStream.writeObject(request);

			try {
				return (Response) this.inputStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Error occured during the List Process in ClientCli");
			}
			return new MessageResponse("ListRequest was not successful");
		}
		return new MessageResponse("You are not logged in");

	}

	@Override
	@Command
	public Response download(String filename) throws IOException {
		if(login) {
			Request request = new DownloadTicketRequest(filename);
			this.outputStream.writeObject(request);

			try {
				Response response = (Response) this.inputStream.readObject();
				DownloadTicketResponse downloadTicketResponse = null;
				if(response instanceof MessageResponse) {
					return response;
				} else if (response instanceof DownloadTicketResponse) {
					downloadTicketResponse = (DownloadTicketResponse) response;
				}
				Socket s = null;
				ObjectOutputStream outputDownloadStream = null;
				ObjectInputStream inputDownloadStream = null;

				try {
					s = new Socket(downloadTicketResponse.getTicket().getAddress(), downloadTicketResponse.getTicket().getPort());
					outputDownloadStream = new ObjectOutputStream(s.getOutputStream());
					inputDownloadStream = new ObjectInputStream(s.getInputStream());

					outputDownloadStream.writeObject(new DownloadFileRequest(downloadTicketResponse.getTicket()));

					Object o = inputDownloadStream.readObject();
					DownloadFileResponse downloadFile = (DownloadFileResponse) o;

					File dir = new File(reader.getDownloadDir());

					File temp = new File(dir, filename);
					FileOutputStream fileInputStream = new FileOutputStream(temp);

					fileInputStream.write(downloadFile.getContent());
					fileInputStream.close();
					return downloadFile;

				} catch (IOException e) {
					e.printStackTrace();
					return new MessageResponse("Irgendwos hot ned passt :(" + e.getMessage());
				} catch(ClassNotFoundException e) {
					System.err.println("ClassNotFounException occured while closing the Socket in ClientCli" +e.getMessage());
				} finally {
					if (s != null) {
						try {
							s.close();
						} catch (IOException e) {
							System.err.println("Error occured while closing the Socket in ClientCli");
						}
					}
					if (outputDownloadStream != null) {
						try {
							outputDownloadStream.close();
						} catch (IOException e) {
							System.err.println("Error occured while closing the ObjectOutputStream in ClientCli");
						}
					}
					if (inputDownloadStream != null) {
						try {
							inputDownloadStream.close();
						} catch (IOException e) {
							System.err.println("Error occured while closing the ObjectInputStream in ClientCli");
						}
					}
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Error occured during the Download Process in ClientCli");
			}
			return new MessageResponse("DownloadRequest was not successful");
		}
		return new MessageResponse("You are not logged in");

	}

	@Override
	@Command
	public MessageResponse upload(String filename) throws IOException {
		if(login) {
			File f = new File(reader.getDownloadDir());
			File[] fileArray = f.listFiles();
			File temp = null;
			for(int i = 0; i < fileArray.length; i++) {
				if(fileArray[i].getName().equals(filename)) {
					temp = fileArray[i];
				}
			}
			if(temp == null) {
				return new MessageResponse("File is not there.");
			}
			FileInputStream fileInputStream = new FileInputStream(temp);
			byte[] data = new byte[(int) temp.length()];
			fileInputStream.read(data);
			fileInputStream.close();
			Request request = new UploadRequest(filename, 1, data);
			this.outputStream.writeObject(request);

			try {
				MessageResponse msg = (MessageResponse) this.inputStream.readObject();
				return new MessageResponse(msg.getMessage() + "\n" + buy(0));
			} catch (ClassNotFoundException e) {
				System.err.println("Error occured during the Upload Process in ClientCli");
			}
			return new MessageResponse("UploadRequest was not successful");
		}
		return new MessageResponse("You are not logged in");

	}

	@Override
	@Command
	public MessageResponse logout() throws IOException {
		if(login) {
			Request request = new LogoutRequest();
			this.outputStream.writeObject(request);

			try {
				login = false;
				
				return (MessageResponse) this.inputStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Error occured during the LogOut Process in ClientCli");
			}
			return new MessageResponse("DownloadRequest was not successful");
		}
		return new MessageResponse("You are not logged in");

	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		this.pool.shutdownNow();
		this.outputStream.close();
		this.inputStream.close();
		this.socket.close();
		this.shell.close();
		return new MessageResponse("Client is terminated");

	}
}
