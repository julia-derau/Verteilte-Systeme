package proxy;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

import util.ChecksumUtils;
import util.Config;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.InfoResponse;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadTicket;

public class Proxy implements IProxy, Runnable, Closeable {
	private Config config;
	private Set<UserInfo> users;
	private UserInfo user;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Set<FileServerInfo> fileservers;

	public Proxy(ProxyReader pReader, Set<UserInfo> users,
			Set<FileServerInfo> fileserver, Socket socket) throws IOException {
		this.config = new Config("user");
		this.users = users;
		this.socket = socket;
		this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
		this.inputStream = new ObjectInputStream(this.socket.getInputStream());
		this.fileservers = fileserver;
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		synchronized(this.users) {
			if(this.users.contains(new UserInfo(request.getUsername(), 0, true))) {
				if (config.getString(request.getUsername() + ".password").equals(request.getPassword())) {
					for (UserInfo u : this.users) {
						if (u.getName().equals(request.getUsername())) {
							this.user = u;
							u.setOnline(true);
							this.users.add(u);
						}

					}
					return new LoginResponse(Type.SUCCESS);
				}
			}
		}
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	public Response credits() throws IOException {
		return new MessageResponse("You have "+this.user.getCredits() + " left.");
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		this.user.setCredits(this.user.getCredits() + credits.getCredits());
		return new MessageResponse("You now have "+this.user.getCredits() + " credits.");
	}

	@Override
	public Response list() {
		FileServerInfo lastFS = getLowestUsageFS();
		if(lastFS == null) {
			return new MessageResponse("No FileServer connected");
		}
		return sendAndReceiveResponse(new ListRequest(), lastFS);
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		FileServerInfo lastFS = getLowestUsageFS();
		if(lastFS == null) {
			return new MessageResponse("No FileServer connected");
		}

		Response vResponse = sendAndReceiveResponse(new VersionRequest(request.getFilename()), lastFS);
		VersionResponse version = null;

		if(vResponse instanceof VersionResponse) {
			version = (VersionResponse) vResponse;
		} else {
			return vResponse;
		}

		Response iResponse = sendAndReceiveResponse(new InfoRequest(request.getFilename()), lastFS);
		InfoResponse info = null;
		if(iResponse instanceof InfoResponse) {
			info = (InfoResponse) iResponse;
		} else {
			return new MessageResponse("Wrong InfoResponse during Download in Proxy");
		}

		if(info.getSize()<= this.user.getCredits()) {
			this.user.setCredits(this.user.getCredits()-info.getSize());
			lastFS.setUsage(lastFS.getUsage() + info.getSize());

			String checkSum = ChecksumUtils.generateChecksum(user.getName(), version.getFilename(), version.getVersion(), info.getSize());
			return new DownloadTicketResponse(new DownloadTicket(this.user.getName(),request.getFilename(), checkSum, lastFS.getAddress(), lastFS.getPort()));
		}
		return new MessageResponse("You don't have enough credits!");
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		synchronized (fileservers) {
			if(this.fileservers.isEmpty()) {
				return new MessageResponse("No FileServer connected");
			}
			for(FileServerInfo temp : this.fileservers) {		
				Object o = sendAndReceiveResponse(request, temp);
			}
			this.user.setCredits(this.user.getCredits()+ 2*request.getContent().length);
			return new MessageResponse("Upload was successful");
		}	
	}

	@Override
	public MessageResponse logout() throws IOException {
		this.user.setOnline(false);
		this.user.setCredits(config.getInt(this.user.getName() + ".credits"));
		return new MessageResponse("Successfully logged out.");
	}

	@Override
	public void run() {
		while (true) {
			Object o = new Object();
			try {
				o = inputStream.readObject();
				if (o instanceof Request) {
					Request request = (Request) o;
					Response response = getResponse(request);
					outputStream.writeObject(response);

				}
			} catch(EOFException e) {
				return;
			} catch (IOException e) {
				return;
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException occurred during reading and writing in Proxy");
			}
		}
	}

	private Response getResponse(Request request) {
		try {
			if (request instanceof BuyRequest) {
				BuyRequest buyRequest = (BuyRequest) request;
				return buy(buyRequest);
			} else if (request instanceof CreditsRequest) {
				return credits();
			} else if (request instanceof DownloadTicketRequest) {
				DownloadTicketRequest dowloadTicketRequest = (DownloadTicketRequest) request;
				return download(dowloadTicketRequest);
			} else if (request instanceof ListRequest) {
				return list();
			} else if (request instanceof LoginRequest) {
				LoginRequest loginRequest = (LoginRequest) request;
				return login(loginRequest);
			} else if (request instanceof LogoutRequest) {
				return logout();
			} else if (request instanceof UploadRequest) {
				UploadRequest uploadRequest = (UploadRequest) request;
				return upload(uploadRequest);
			}
		} catch (IOException e) {
			System.err.println("InstanceOf of the Request in Proxy went wrong");
		}
		return new MessageResponse("Unsupported Request");

	}

	private FileServerInfo getLowestUsageFS() {
		if(fileservers.isEmpty()) {
			return null;
		}
		synchronized (fileservers) {
			FileServerInfo lastFs = null;
			for (FileServerInfo f : this.fileservers) {
				if (f.isOnline() && lastFs == null) {
					lastFs = f;
				}
				if (f.isOnline() && lastFs.getUsage() > f.getUsage()) {
					lastFs = f;
				}
			}
			return lastFs;
		}

	}
	private Response sendAndReceiveResponse(Request request, FileServerInfo fs) {
		Socket socketFS = null;
		ObjectOutputStream outputFsStream = null;
		ObjectInputStream inputFsStream = null;

		try {
			socketFS = new Socket(fs.getAddress(), fs.getPort());

			outputFsStream = new ObjectOutputStream(socketFS.getOutputStream());
			inputFsStream = new ObjectInputStream(socketFS.getInputStream());

			outputFsStream.writeObject(request);

			Object o = inputFsStream.readObject();
			if(o instanceof Response) {
				return (Response) o;
			}

		} catch (ClassNotFoundException e) {
			System.err.println("IOException occured during the Upload in Proxy");
		} catch (IOException e) {
			System.err.println("IOException occured during the Upload in Proxy");
		} finally {
			if (socketFS != null) {
				try {
					socketFS.close();
				} catch (IOException e) {
					System.err.println("Error occured while closing the ObjectOutputStream in Proxy");
				}
			}
			if (outputFsStream != null) {
				try {
					outputFsStream.close();
				} catch (IOException e) {
					System.err.println("Error occured while closing the ObjectOutputStream in Proxy");
				}
			}
			if (inputFsStream != null) {
				try {
					inputFsStream.close();
				} catch (IOException e) {
					System.err.println("Error occured while closing the ObjectInputStream in Proxy");
				}
			}
		}
		return new MessageResponse("No correct answer from FileServer to the Proxy");

	}
	@Override
	public void close() throws IOException {
		if(this.inputStream != null) {
			this.inputStream.close();
		}
		if(this.outputStream != null) {
			this.outputStream.close();
		}
		if(this.socket != null) {
			if(!socket.isClosed()) {
				this.socket.close();
			}
		}
	}
}
