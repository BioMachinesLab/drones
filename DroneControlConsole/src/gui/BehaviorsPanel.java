package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import commoninterface.CIBehavior;

import network.messages.BehaviorMessage;
import threads.UpdateThread;
import utils.ClassLoadHelper;

public class BehaviorsPanel extends UpdatePanel{
	
	private UpdateThread thread;
	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	
	public BehaviorsPanel() {
		
		setBorder(BorderFactory.createTitledBorder("Behaviors"));
		
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		
		topPanel.setLayout(new GridLayout(5,2));
		
		JComboBox<Class<CIBehavior>> behaviors = new JComboBox<>();
		behaviors.setPreferredSize(new Dimension(20,20));
		
		populateBehaviors(behaviors);
		
		topPanel.add(behaviors);
		
		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((Class<CIBehavior>)behaviors.getSelectedItem(), true);
			}
		});
		
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((Class<CIBehavior>)behaviors.getSelectedItem(), false);
			}
		});
		
		topPanel.add(start);
		topPanel.add(new JLabel(""));
		topPanel.add(stop);
		
		JTextField argIndex = new JTextField(5);
		JTextField argValue = new JTextField(5);
		
		JButton argumentButton = new JButton("Set Argument");
		
		argumentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int index = Integer.parseInt(argIndex.getText());
					double value = Double.parseDouble(argValue.getText());
					
					argumentMessage((Class<CIBehavior>)behaviors.getSelectedItem(), index, value);
				
				} catch(Exception ex){
					statusMessage.setText("Arguments not well formatted!");
				}
			}
		});
		
		topPanel.add(new JLabel("Index"));
		topPanel.add(argIndex);
		
		topPanel.add(new JLabel("Value"));
		topPanel.add(argValue);
		
		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10,20));
		
		topPanel.add(new JLabel());
		topPanel.add(argumentButton);
		
		add(topPanel, BorderLayout.NORTH);
		add(statusMessage, BorderLayout.SOUTH);
	}
	
	private synchronized void statusMessage(Class<CIBehavior> className, boolean status) {
		statusMessage.setText("");
		currentMessage = new BehaviorMessage(className, status);
		notifyAll();
	}
	
	private synchronized void argumentMessage(Class<CIBehavior> className, int index, double value) {
		statusMessage.setText("");
		currentMessage = new BehaviorMessage(className, index, value);
		notifyAll();
	}
	
	private void populateBehaviors(JComboBox<Class<CIBehavior>> list) {
		ArrayList<Class<?>> classes = ClassLoadHelper.findRelatedClasses(CIBehavior.class);
		for(Class<?> c : classes) {
			list.addItem((Class<CIBehavior>)c);
		}
	}
	
	public BehaviorMessage getCurrentMessage() {
		BehaviorMessage result = currentMessage;
		currentMessage = null;
		return result;
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	@Override
	public synchronized void threadWait() {
		while(currentMessage == null) {
			try {
				wait();
			} catch(Exception e){}
		}
	}
	
	@Override
	public long getSleepTime() {
		return 0;
	}

	public void displayData(BehaviorMessage message) {
		String result = message.getSelectedBehavior().getSimpleName()+": ";
		result+=message.changeStatusOrder() ?
				(message.getSelectedStatus() ? "start" : "stop") :
					message.getArgumentIndex()+"="+message.getArgumentValue();
		
		statusMessage.setText(result);
	}
}