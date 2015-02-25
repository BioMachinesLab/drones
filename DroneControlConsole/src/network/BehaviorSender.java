package network;

import gui.panels.BehaviorsPanel;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import network.messages.BehaviorMessage;

public class BehaviorSender extends Thread{
	
	private static int BEHAVIOR_PORT = 10103;
	
	private String[] ips;
	private BehaviorMessage message;
	private BehaviorsPanel bp;
	
	public BehaviorSender(BehaviorMessage message, String[] ips, BehaviorsPanel bp) {
		this.ips = ips;
		this.message = message;
		this.bp = bp;
	}
	
	@Override
	public void run() {
		
		SenderThread[] sts = new SenderThread[ips.length];
		int i = 0;
		for(String ip : ips) {
			try {
				sts[i] = new SenderThread(ip);
				sts[i].start();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		int count =  0;
		
		for(SenderThread st : sts) {
			try {
				st.join();
				count++;
				bp.setText("Deploying... "+count+"/"+sts.length);
			} catch(Exception e ) {
				e.printStackTrace();
			}
		}
		
		bp.setText("Deployed!");
	}
	
	class SenderThread extends Thread {
		
		private Socket socket;
		
		public SenderThread(String ip) throws IOException{
			socket = new Socket(ip, BEHAVIOR_PORT);
		} 
		
		@Override
		public void run() {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				out.writeObject(message);
				out.reset();
				out.flush();
				Object o = in.readObject();
				
				if(o == null || !(o instanceof BehaviorMessage)) {
					System.out.println("[BehaviorSender] Didn't receive the right confirmation!");
				}
				
				out.close();
				in.close();
				socket.close();
				
			} catch(Exception e ) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch(Exception e){e.printStackTrace();}
			}
		}
	}
}
