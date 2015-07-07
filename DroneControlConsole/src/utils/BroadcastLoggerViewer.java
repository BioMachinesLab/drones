package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JLabel;

import network.broadcast.ConsoleBroadcastHandler;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class BroadcastLoggerViewer extends JFrame {

	private static String FOLDER = "broadcast_logs";
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH-mm-ss");
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	
	private BufferedWriter bw = null;
	
	private JLabel label = new JLabel();
	
	public BroadcastLoggerViewer() {
		
		super("Broadcast Message Logger");
		
		String fileName = DateTime.now().toString(dateFormatter)+".log";
		
		try {
			
			File folder = new File(FOLDER);
			
			if(!folder.exists()) {
				folder.mkdir();
			}
			
			bw = new BufferedWriter(new FileWriter(new File(FOLDER+"/"+fileName)));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if(bw != null) {
					try {
						bw.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		label.setText("Logging to "+fileName);
		
		new BroadcastReceiver().start();
		
		add(label);
		setSize(600, 100);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public void log(String msg) {
		if(bw != null) {
			try {
				String txt = DateTime.now().toString(hourFormatter)+" "+msg;
				label.setText(txt);
				bw.append(txt+"\n");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class BroadcastReceiver extends Thread {
		
		private DatagramSocket socket;

		public BroadcastReceiver() {
			try {
				System.out.println("RECEIVER " + InetAddress.getByName("0.0.0.0") + ", port: " + ConsoleBroadcastHandler.PORT);
				 socket = new DatagramSocket(ConsoleBroadcastHandler.PORT, InetAddress.getByName("0.0.0.0"));
				 socket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			try {
				while (true) {
					byte[] recvBuf = new byte[ConsoleBroadcastHandler.BUFFER_LENGTH];
					DatagramPacket packet = new DatagramPacket(recvBuf,recvBuf.length);
					socket.receive(packet);
					String message = new String(packet.getData()).trim();
					
					log(packet.getAddress().getHostAddress()+" "+message);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new BroadcastLoggerViewer();
	}
	
}
