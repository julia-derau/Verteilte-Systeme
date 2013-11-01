package proxy;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import cli.Command;
import cli.Shell;

import util.ChecksumUtils;
import util.Config;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.LoginResponse.Type;
import message.response.MessageResponse;

public class Proxy implements IProxy, Runnable, Closeable {
	private Shell shell;
	private ProxyReader pReader;
	private Config config;
	private Set<UserInfo> users;
	private UserInfo user;
	private Set<File> files;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Socket socketProxy;
	private Set<FileServerInfo> fileservers;

	public Proxy(Shell shell, ProxyReader pReader, Set<UserInfo> users,
			Set<FileServerInfo> fileserver, Socket socket) throws IOException {
		this.config = new Config("user");
		this.shell = shell;
		this.pReader = pReader;
		this.users = users;
		this.files = new HashSet<File>();
		this.socket = socket;
		this.outputStream = new ObjectOutputStream(
				this.socket.getOutputStream());
		this.inputStream = new ObjectInputStream(this.socket.getInputStream());
		this.fileservers = fileserver;

	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		if (config.getString(request.getUsername() + ".password").equals(
				request.getPassword())) {
			for (UserInfo u : users) {
				if (u.getName().equals(request.getUsername())) {
					this.user = u;
					u.setOnline(true);
					this.users.add(u);
				}
			}
			return new LoginResponse(Type.SUCCESS);
		}
		return new LoginResponse(Type.WRONG_CREDENTIALS);
	}

	@Override
	public Response credits() throws IOException {
		return new CreditsResponse(this.user.getCredits());
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		this.user.setCredits(this.user.getCredits() - credits.getCredits());
		return new BuyResponse(this.user.getCredits());
	}

	@Override
	public Response list() {
		FileServerInfo temp = getLowestUsageFS();
		Socket s = null;
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;

		try {
			System.out.println("temp.getaddress"+temp.getAddress() + "temp.getPort"+temp.getPort());
			s = new Socket(temp.getAddress(), temp.getPort());
			System.out.println(s.getPort());
			outputStream = new ObjectOutputStream(s.getOutputStream());
			inputStream = new ObjectInputStream(s.getInputStream());
			System.out.println("komm ich hier her?");

			outputStream.writeObject(new ListRequest());
			System.out.println("komm ich hier her oder hier?");

			Object o = inputStream.readObject();
			System.out.println("object o??" + o);
			return (ListResponse) o;
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
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {

		/**
		 * File temp = null; for(File f : this.files) {
		 * if(f.getName().equals(request.getFilename())) { temp = f; } } String
		 * s = ChecksumUtils.generateChecksum(this.user.getName(),
		 * request.getFilename(), 1, temp.length()); request. return new
		 * DownloadTicketResponse(ticket)
		 **/
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageResponse logout() throws IOException {
		this.user.setOnline(false);
		return new MessageResponse("User" + user.getName() + "logged out");
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
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("Unsupported Request");

	}

	private FileServerInfo getLowestUsageFS() {
		int pointer = 0;
		long usage = 0;
		FileServerInfo fs = null;
		for (FileServerInfo f : this.fileservers) {
			if (pointer == 0) {
				usage = f.getUsage();
				fs = f;
			}
			if (usage < f.getUsage()) {
				usage = f.getUsage();
				fs = f;
			}
		}
		return fs;
	}

	@Override
	public void close() throws IOException {

		// this.socket.close();
		// this.shell.close();
	}
}
