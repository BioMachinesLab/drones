package io.input;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.CommandLine;

import commoninterface.RobotCI;
import commoninterface.network.messages.CameraCaptureMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;

public class CameraCaptureInput implements ControllerInput, MessageProvider {
	private static final int PORT = 20101;

	private CameraThread cameraThread;
	protected boolean available = false;

	private RobotCI robotCI;

	public CameraCaptureInput(RobotCI robotCI) {
		this.robotCI = robotCI;
		available = true;
		CommandLine.executeShellCommand("./picamera/init_server.sh");

		cameraThread = new CameraThread();
		cameraThread.start();
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.CAMERA_CAPTURE)) {
			return new CameraCaptureMessage((byte[]) getReadings(),
					robotCI.getNetworkAddress());
		}
		return null;
	}

	@Override
	public Object getReadings() {
		if (cameraThread != null)
			return cameraThread.getImageBytes();
		else
			return null;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Convert file into array of bytes
	 * 
	 * @param file
	 *            - File to be converted into bytes
	 * @return Array of bytes of the converted file
	 * 
	 */
	public byte[] convertFileToBytes(File file) throws IOException {
		byte[] bytes = new byte[(int) file.length()];
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(bytes);
		fileInputStream.close();
		return bytes;
	}

	class CameraThread extends Thread {
		private Socket socket;
		private InputStream inputStream;

		private byte[] imageBytes;

		public CameraThread() {
		}

		@Override
		public void run() {
			try {

				Thread.sleep(5000);

				socket = new Socket("127.0.0.1", PORT);
				inputStream = socket.getInputStream();

				System.out.println("[INIT] Picamera ready!");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ByteArrayOutputStream extraBytes = new ByteArrayOutputStream();

				// int time = 0;
				// long startTime = 0;

				while (true) {

					// if(time == 1){
					// startTime = System.currentTimeMillis();
					// }

					byte[] content = new byte[2048];
					int bytesRead = -1;

					boolean ok = true;

					boolean gotEnd = false;
					boolean overflowed = false;

					do {
						bytesRead = inputStream.read(content);

						if (bytesRead == -1) {
							ok = false;
						} else {
							for (int i = 0; i < bytesRead - 1; i++) {
								if (content[i] == -1 && content[i + 1] == -39) {
									gotEnd = true;
									if (i + 2 < bytesRead) {
										int originalBytes = i + 2;
										baos.write(content, 0,
												originalBytes + 1);
										overflowed = true;
										extraBytes.write(content,
												originalBytes, bytesRead
														- originalBytes);

									}
									break;
								}
							}

							if (!overflowed) {
								baos.write(content, 0, bytesRead);
							}

						}

					} while (ok && !gotEnd);

					imageBytes = baos.toByteArray();

					baos = new ByteArrayOutputStream();

					baos.write(extraBytes.toByteArray());
					extraBytes = new ByteArrayOutputStream();

					// time ++;

					// if(time > 1){
					// double t = System.currentTimeMillis()-startTime;
					// t/=1000;
					//
					// System.out.println("Captures: "+time);
					// System.out.println("FPS: "+(time/t));
					// }

				}

			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.err.println("Unknown Host.");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Couldn't get I/O for  the connection.");
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.err.println("Image thread interrupted.");
			} finally {
				try {
					inputStream.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public byte[] getImageBytes() {
			return imageBytes;
		}

	}

}