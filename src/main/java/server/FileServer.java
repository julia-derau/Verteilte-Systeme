package server;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import util.ChecksumUtils;

import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DownloadFileResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;

public class FileServer implements IFileServer, Runnable, Closeable {
	private Set<String> filenames;
	private Set<File> files = new HashSet<File>();
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private Socket socket;
	private FileServerReader reader;

	public FileServer(FileServerReader reader, Socket socket) {
		this.filenames = new HashSet<String>();
		this.socket = socket;
		this.reader=reader;

		try {
			this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
			this.inputStream = new ObjectInputStream(this.socket.getInputStream());

		} catch (IOException e) {
			System.err.println("Error occured during the instanziation of the ObjectOutput/Input - Stream in Fileserver");
		}
	}

	@Override
	public Response list() throws IOException {
		File f = new File(this.reader.getFileServerDir());
		File[] fileArray = f.listFiles();
		for(int i = 0; i < fileArray.length; i++) {
			this.filenames.add(fileArray[i].getName());
			this.files.add(fileArray[i]);
		}
		return new ListResponse(this.filenames);
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		list();

		for(File f : this.files) {
			if(f.getName().equals(request.getTicket().getFilename())) {
				VersionResponse response = (VersionResponse) version(new VersionRequest(f.getName()));
				String checkSum = ChecksumUtils.generateChecksum(request.getTicket().getUsername(), f.getName(), response.getVersion(), f.length());
				if(ChecksumUtils.verifyChecksum(request.getTicket().getUsername(), f, 1, checkSum)) {
					FileInputStream fileInputStream = new FileInputStream(f);
					byte[] data = new byte[(int) f.length()];
					fileInputStream.read(data);
					fileInputStream.close();
					return new DownloadFileResponse(request.getTicket(), data);
				}
			}
		}
		return new MessageResponse("File not found!");
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		list();
		for(File f : this.files) {
			if(f.getName().equals(request.getFilename())) {
				return new InfoResponse(request.getFilename(), f.length());
			}
		}
		return null;
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		list();
		for(String f : this.filenames) {
			if(f.equals(request.getFilename())) {
				return new VersionResponse(request.getFilename(), 1);
			}
		}
		return new MessageResponse("File is not there.");
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		File dir = new File(reader.getFileServerDir());

		File temp = new File(dir, request.getFilename());
		FileOutputStream fileInputStream = new FileOutputStream(temp);

		fileInputStream.write(request.getContent());
		fileInputStream.close();

		return new MessageResponse("File successfully uploaded.");
	}

	@Override
	public void run() {
		while(true) {
			try {
				Object o = inputStream.readObject();

				if (o instanceof Request) {
					Request request = (Request) o;
					Response response = getResponse(request);

					outputStream.writeObject(response);

				}

			} catch(EOFException e) {
				return;
			} catch (IOException e) {
				return;
			} catch(ClassNotFoundException e) {
				System.err.println("Error occured while reading/writing at the ObjectOutput/Input - Stream in FileServer");
				return;
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						System.err.println("Error occured while closing the ObjectOutputStream in FileServer");
					}
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						System.err.println("Error occured while closing the ObjectInputStream in FileServer");
					}
				}
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
				return list();
			} else if (request instanceof VersionRequest) {
				VersionRequest versionRequest = (VersionRequest) request;
				return version(versionRequest);
			} else if (request instanceof InfoRequest) {
				InfoRequest infoRequest = (InfoRequest) request;
				return info(infoRequest);
			}
		} catch (IOException e) {
			System.err.println("Error occured while instanceof of the Response in FileServer");
		}
		return new MessageResponse("Error");
	}

	@Override
	public void close() throws IOException {
		if(!socket.isClosed()) {
			socket.close();
		}

	}

}
