package server;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import cli.Command;

public class FileServer implements IFileServer, Runnable {
	private Set<String> filenames;
	private Set<File> files = new HashSet<File>();
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private Socket socket;

	public FileServer(FileServerReader reader, Socket socket) {
		this.filenames = new HashSet<String>();
		this.socket = socket;
		System.out.println("port"+socket.getPort());
		try {
			this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
			this.inputStream = new ObjectInputStream(this.socket.getInputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Response list() throws IOException {
		File f = new File("files/fileserver1");
		File[] fileArray = f.listFiles();
		for(int i = 0; i < fileArray.length; i++) {
			this.filenames.add(fileArray[i].getName());
			this.files.add(fileArray[i]);
		}
		return new ListResponse(this.filenames);
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		for(File f : this.files) {
			if(f.getName().equals(request.getFilename())) {
				return new InfoResponse(request.getFilename(), f.length());
			}
		}
		return null;
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		for(File f : this.files) {
			if(f.getName().equals(request.getFilename())) {
				//TODO VERSION NUMMER?
				return new VersionResponse(request.getFilename(), f.hashCode());
			}
		}
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		while(true) {
			try {
				System.out.println("hier");

				Object o = inputStream.readObject();
				System.out.println("objekt:"+o);

				if (o instanceof Request) {
					Request request = (Request) o;
					Response response = getResponse(request);
					System.out.println("antwort senden:");
					System.out.println(response);

					outputStream.writeObject(response);
					System.out.println("antwort gesendet:");

				}

			} catch(EOFException e) {
				return;
			}catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

			}
		}

	}
	private Response getResponse(Request request) {
		try {
			if (request instanceof UploadRequest) {
				UploadRequest uploadRequest = (UploadRequest) request;
				return upload(uploadRequest);
			} else if (request instanceof DownloadFileRequest) {
				DownloadFileRequest downloadRequest = (DownloadFileRequest) request;
				return download(downloadRequest);
			} else if (request instanceof ListRequest) {
				System.out.println("bin ich eine liste?");
				return list();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("Error");
	}

}
