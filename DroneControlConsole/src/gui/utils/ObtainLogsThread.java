package gui.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ObtainLogsThread extends Thread{

	private static final String DRONES_LOGS_FOLDER = "/home/pi/RaspberryController/logs";

	private String address;

	public ObtainLogsThread(String address) {
		this.address = address;
	}

	@Override
	public void run() {
		try {
			JSch jsch = new JSch();

			Session session = jsch.getSession("pi", address, 22);
			session.setPassword("raspberry");
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			getFileTrhoughSFTP(session, address);

			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getFileTrhoughSFTP(Session session, String address) throws JSchException, SftpException, IOException{
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();

		String[] split = address.split("\\.");
		File dir = new File("drones_logs/" + split[split.length-1]);

		if(!dir.exists())
			dir.mkdirs();

		@SuppressWarnings("unchecked")
		Vector<LsEntry> ls = sftpChannel.ls(DRONES_LOGS_FOLDER);

		for (LsEntry entry : ls) {
			String fileName = entry.getFilename();
			if(fileName.endsWith("log")){
				InputStream in = sftpChannel.get(DRONES_LOGS_FOLDER + "/" + fileName);
				File f = new File(dir.getAbsolutePath() + "/" + fileName);

				if(!f.exists()){
					Path destination = Paths.get(f.getAbsolutePath());
					Files.copy(in, destination);
				}

				in.close();
			}
		}
		sftpChannel.disconnect();
	}

}