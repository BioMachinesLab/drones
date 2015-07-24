package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import commoninterface.network.NetworkUtils;

public class BroadcastLogPlayback extends JFrame {
	
	protected int PORT = 8988;
	protected int SEND_PORT = 8888;
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	private String file = "broadcast_logs/expo22july.log";
	
	private JSlider slider;
	private int currentStep = 0;
	private PlayThread playThread;
	private JLabel currentStepLabel;
	
	private int lastIncrementStep = 0;
	
	private String[] data;
	
	private BroadcastSender bs;
	
	public BroadcastLogPlayback() {
		
		int nData = countLines();
		data = new String[nData];
		
		System.out.println("Lines: "+nData);
		
		readData();
		
		//TODO debug
		fetchData();
		
		playThread = new PlayThread();
		playThread.start();
		
		try {
			bs = new BroadcastSender(InetAddress.getByName(NetworkUtils.getAddress()), PORT);
		} catch(Exception e){
			e.printStackTrace();
		}
		
		slider = new JSlider(0,nData);
		slider.setValue(0);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				currentStep = slider.getValue();
				slider.setToolTipText(""+slider.getValue());
				if(!playThread.isPlaying())
					moveTo(slider.getValue());
			}
		});
		
		JButton playButton = new JButton("Play/Pause");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playThread.toggle();
			}
		});
		
		currentStepLabel = new JLabel();
		updateCurrentStepLabel();
		
		JButton slower = new JButton("Speed --");
		slower.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playThread.playSlower();
			}
		});
		
		JButton faster = new JButton("Speed ++");
		faster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playThread.playFaster();
			}
		});
		
		JPanel controlsPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel();
		
		buttonsPanel.add(slower);
		buttonsPanel.add(playButton);
		buttonsPanel.add(faster);
		
		controlsPanel.add(currentStepLabel, BorderLayout.NORTH);
		controlsPanel.add(slider, BorderLayout.CENTER);
		controlsPanel.add(buttonsPanel, BorderLayout.SOUTH);
		
		add(controlsPanel,BorderLayout.SOUTH);
		
		setSize(800, 120);
		setVisible(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void fetchData() {
		
		DateTime start = DateTime.parse("12:14:59.00", hourFormatter);
		DateTime end = DateTime.parse("12:17:59.00", hourFormatter);
		
		System.out.println();
		
		for(String s : data) {
			
			String[] split = s.split(" ");
			DateTime d = DateTime.parse(split[0], hourFormatter);
			
			if(d.isAfter(start) && d.isBefore(end) && s.contains("GPS"))
				System.out.println(s);
			
			if(d.isAfter(end)) {
				System.exit(0);
			}
			
		}
	}
	
	private int countLines() {
		int n = 0;
		try {
			Scanner s = new Scanner(new File(file));
			
			while(s.hasNextLine()) {
				n++;
				s.nextLine();
			}
			s.close();
		} catch(Exception e){}
		return n;
	}
	
	private void updateCurrentStepLabel() {
		
		if(currentStep < data.length) {
			
			String text = "Step: "+currentStep+"/"+data.length;
			text+="\t Time: "+(data[currentStep].split(" ")[0])+" ("+(1/playThread.getMultiplier())+"x)";
			currentStepLabel.setText(text);
		}
	}
	
	private void moveTo(int step) {
		
		if(step > data.length){
			playThread.pause();
			return;
		}
		
		currentStep = step;
		
		for(int i = 0 ; i < step ; i++) {
			bs.sendMessage(data[i].split(" ")[2]);
		}
		
		updateCurrentStepLabel();
	}
	
	private void incrementPlay() {
		
		if(currentStep + 1 > data.length){
			playThread.pause();
			return;
		}
		
		if(lastIncrementStep == currentStep) {
			currentStep++;
			slider.setValue(currentStep);
			bs.sendMessage(data[currentStep].split(" ")[2]);
			
			updateCurrentStepLabel();
		} else {
			moveTo(currentStep);
		}
		
		lastIncrementStep = currentStep;
	}
	
	public static void main(String[] args) {
		new BroadcastLogPlayback();
	}
	
	public void readData() {
		
		System.out.println("Reading data...");
		
		try {
			
			Scanner s = new Scanner(new File(file));
			
			for(int i = 0 ; i < data.length ; i++) {
				
				if(i % 1000 == 0) {
					System.out.print(".");
				}
				if(i % 100000 == 0 && i > 100)
					System.out.println();
				
				data[i] = s.nextLine();
			}
			
			s.close();
		
		} catch(IOException e) {
			e.printStackTrace();
		} 
	}
	
	private long compareTimeWithNextStep() {
		
		if(currentStep + 1 < data.length) {
			DateTime d1 = DateTime.parse(data[currentStep].split(" ")[0],hourFormatter);
			DateTime d2 = DateTime.parse(data[currentStep+1].split(" ")[0],hourFormatter);
			return d2.getMillis() - d1.getMillis();
		}
		
		return 0;
	}
	
	public class PlayThread extends Thread {
		
		private boolean play = false;
		private double multiplier = 1.0;
		
		@Override
		public void run() {
			
			while(true) {
				
				try {
					synchronized(this) {
						if(!play)
							wait();
					}
					
					incrementPlay();
					
					long time = (long)(compareTimeWithNextStep()*multiplier);
					
					if(time > 1000)
						time = 1000;
					
					Thread.sleep(time);
					
				} catch(Exception e){}
			}
		}
		
		public synchronized void play() {
			play = true;
			notify();
		}
		
		public void toggle() {
			if(play)
				pause();
			else
				play();
		}
		
		public void pause() {
			play = false;
		}
		
		public void playFaster() {
			multiplier*=0.5;
			updateCurrentStepLabel();
			interrupt();
		}
		
		public void playSlower() {
			if(multiplier < Math.pow(2, 4)) {
				multiplier*=2;
				updateCurrentStepLabel();
				interrupt();
			}
		}
		
		public boolean isPlaying() {
			return play;
		}
		
		public double getMultiplier() {
			return multiplier;
		}
	}
	
	class BroadcastSender {

		private DatagramSocket socket;

		public BroadcastSender(InetAddress ownAddress, int port) {
			try {
				socket = new DatagramSocket(port+1, ownAddress);
				socket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String message) {
			try {
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, InetAddress.getByName("255.255.255.255"),
						SEND_PORT);
				socket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void stopExecution() {
			if(socket != null)
				socket.close();
		}
	}
}