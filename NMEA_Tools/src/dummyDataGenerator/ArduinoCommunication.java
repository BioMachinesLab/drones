package dummyDataGenerator;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.Enumeration;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

public class ArduinoCommunication implements Runnable {

	private SerialPort serialPort;

	// private BufferedReader input;
	private BufferedWriter output;
	private static final int TIME_OUT = 2000;
	private int baudRate = 115200;
	private static final int SEND_FREQUENCY = 1; // In Hertz

	@SuppressWarnings("unchecked")
	public void initializeCommunication() throws NullPointerException {
		String portName = comPortSelectionWindow(CommPortIdentifier
				.getPortIdentifiers());
		if (portName == null)
			throw new NullPointerException();

		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
				.getPortIdentifiers();
		CommPortIdentifier portId = null;
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			if (currPortId.getName().equals(portName)) {
				System.out.println("Arduino on COM port "
						+ currPortId.getName() + " with BAUD rate of "
						+ baudRate);
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			new JOptionPane("The selected COM port was not found!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// input = new BufferedReader(new InputStreamReader(
			// serialPort.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(
					serialPort.getOutputStream()));

			Thread.sleep(2000);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	private String comPortSelectionWindow(
			Enumeration<CommPortIdentifier> portEnum) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));

		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Please select Arduino COM Port:"));

		JComboBox<String> comboBox = new JComboBox<String>();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			comboBox.addItem(currPortId.getName());
		}
		northPanel.add(comboBox);

		JPanel southPanel = new JPanel();
		southPanel.add(new JLabel("Please insert BAUD rate:"));

		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(300);
		formatter.setMaximum(115200);
		formatter.setCommitsOnValidEdit(true);
		JFormattedTextField field = new JFormattedTextField(formatter);
		field.setText(Integer.toString(baudRate));
		field.setColumns(7);
		field.setHorizontalAlignment(JFormattedTextField.RIGHT);
		southPanel.add(field);

		panel.add(northPanel);
		panel.add(southPanel);

		int result = JOptionPane.showConfirmDialog(null, panel, "COM Port",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		switch (result) {
		case JOptionPane.OK_OPTION:
			baudRate = Integer.parseInt(field.getText());
			return (String) comboBox.getSelectedItem();
		default:
			return null;
		}
	}

	public void sendData(DataToArduino data) throws InterruptedException {
		while (true) {
			try {
				String sentence = data.getSentence();
				output.write(sentence);
				output.flush();

				System.out.println(sentence);
				Thread.sleep((1 / SEND_FREQUENCY) * 1000);
			} catch (IOException e) {
				System.err.println("Error sending to Arduino!");
			}
		}
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			System.out.println("Connection Closed!");
		}
	}

	@Override
	public void run() {
		try {
			initializeCommunication();
			sendData(new DataToArduino());
		} catch (InterruptedException e) {
			System.out.println("Interrupted the generator!");
		} catch (NullPointerException e) {
			new JOptionPane("No Arduino connected to the COM port!",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			close();
		}
	}

	public static void main(String[] args) {
		Thread t = new Thread(new ArduinoCommunication());
		t.start();
	}
}