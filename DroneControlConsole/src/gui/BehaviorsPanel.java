package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import commoninterface.CIBehavior;
import commoninterface.utils.CIArguments;
import network.messages.BehaviorMessage;
import threads.UpdateThread;
import utils.ClassLoadHelper;

public class BehaviorsPanel extends UpdatePanel{
	
	private UpdateThread thread;
	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	private JTextArea config;
	
	public BehaviorsPanel() {
		
		setBorder(BorderFactory.createTitledBorder("Behaviors"));
		
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		
		topPanel.setLayout(new BorderLayout());
		
		JComboBox<Class<CIBehavior>> behaviors = new JComboBox<>();
		behaviors.setPreferredSize(new Dimension(20,20));
		
		populateBehaviors(behaviors);
		
		topPanel.add(behaviors, BorderLayout.NORTH);
		
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
		
		JPanel buttons = new JPanel();
		buttons.add(start);
		buttons.add(stop);
		
		topPanel.add(buttons, BorderLayout.SOUTH);
		
		config = new JTextArea(10,10);
		JScrollPane scroll = new JScrollPane(config);
		
		topPanel.add(scroll,BorderLayout.CENTER);
		
		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10,20));
		
		add(topPanel, BorderLayout.NORTH);
		add(statusMessage, BorderLayout.SOUTH);
	}
	
	private synchronized void statusMessage(Class<CIBehavior> className, boolean status) {
		statusMessage.setText("");
		
		CIArguments translatedArgs = new CIArguments(config.getText().replaceAll("\\s+",""),true);
		
		if(status)
			currentMessage = new BehaviorMessage(className, translatedArgs.getCompleteArgumentString(), status);
		else
			currentMessage = new BehaviorMessage(className, "", status);
		
		notifyAll();
	}
	
	//TODO debug
	public static int sizeof(Object obj) {
		try {
		    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
	
		    objectOutputStream.writeObject(obj);
		    objectOutputStream.flush();
		    objectOutputStream.close();
	
		    return byteOutputStream.toByteArray().length;
		} catch(Exception e){}
		
		return 0;
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
		result+= message.getSelectedStatus() ? "start" : "stop";
		
		statusMessage.setText(result);
	}
}