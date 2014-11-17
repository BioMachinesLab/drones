package gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import network.messages.BehaviorMessage;
import threads.UpdateThread;
import utils.ClassLoadHelper;
import behaviors.Behavior;

public class BehaviorsPanel extends UpdatePanel{
	
	private UpdateThread thread;
	private int sleepTime = 10;
	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	
	public BehaviorsPanel() {
		
		setLayout(new GridLayout(5,2));
		
		setBorder(BorderFactory.createTitledBorder("Behaviors"));
		
		JComboBox<Class<Behavior>> behaviors = new JComboBox<>();
		behaviors.setPreferredSize(new Dimension(20,20));
		
		populateBehaviors(behaviors);
		
		add(behaviors);
		
		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((Class<Behavior>)behaviors.getSelectedItem(), true);
			}
		});
		
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((Class<Behavior>)behaviors.getSelectedItem(), false);
			}
		});
		
		add(start);
		add(new JLabel(""));
		add(stop);
		
		JTextField argIndex = new JTextField(5);
		JTextField argValue = new JTextField(5);
		
		JButton argumentButton = new JButton("Set Argument");
		
		argumentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int index = Integer.parseInt(argIndex.getText());
					double value = Double.parseDouble(argValue.getText());
					
					argumentMessage((Class<Behavior>)behaviors.getSelectedItem(), index, value);
				
				} catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Arguments not well formatted!");
				}
			}
		});
		
		add(new JLabel("Index"));
		add(argIndex);
		
		add(new JLabel("Value"));
		add(argValue);
		
		statusMessage = new JLabel();
		
		add(statusMessage);
		add(argumentButton);
	}
	
	private synchronized void statusMessage(Class<Behavior> className, boolean status) {
		currentMessage = new BehaviorMessage(className, status);
		notifyAll();
	}
	
	private synchronized void argumentMessage(Class<Behavior> className, int index, double value) {
		currentMessage = new BehaviorMessage(className, index, value);
		notifyAll();
	}
	
	private void populateBehaviors(JComboBox<Class<Behavior>> list) {
		ArrayList<Class<?>> classes = ClassLoadHelper.findRelatedClasses(Behavior.class);
		for(Class<?> c : classes) {
			list.addItem((Class<Behavior>)c);
		}
	}
	
	public BehaviorMessage getCurrentMessage() {
		
		while(currentMessage == null) {
			try {
				wait();
			} catch(Exception e){}
		}
		
		return currentMessage;
	}
	

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	@Override
	public synchronized void threadSleep() {
		try {
			wait();
			Thread.sleep(sleepTime);
		}catch(Exception e) {}
	}
}
