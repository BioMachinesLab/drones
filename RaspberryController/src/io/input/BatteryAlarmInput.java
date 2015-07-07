package io.input;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import commoninterface.RobotCI;
import commoninterface.dataobjects.BatteryStatus;
import commoninterface.network.messages.BatteryMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;

public class BatteryAlarmInput extends Thread implements ControllerInput,
		MessageProvider {
	private static int HISTORY_SIZE = 25;
	private static int UPDATE_RATE = 2; // In Hertz
	private static final Pin ALARM_PIN = RaspiPin.GPIO_06;

	private GpioPinDigitalInput alarmInputPin;

	private boolean available = false;
	private RobotCI robotCI;
	private ArrayCircularQueue circularQueue;

	public BatteryAlarmInput(GpioController gpioController, RobotCI robotCI) {
		this.robotCI = robotCI;

		alarmInputPin = gpioController.provisionDigitalInputPin(ALARM_PIN);
		available = true;
		circularQueue = new ArrayCircularQueue(HISTORY_SIZE);
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public Object getReadings() {
		return (circularQueue.calculateMean() > 0.5) ? true : false;
	}

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (this) {
					try {
						circularQueue.insert(alarmInputPin.isHigh() ? 1 : 0);
						sleep((long) ((1.0 / UPDATE_RATE) * 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			alarmInputPin.setShutdownOptions(true, PinState.LOW,
					PinPullResistance.OFF);
		}
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.BATTERY)) {
			if (!available) {
				return new SystemStatusMessage(
						"[AlarmInput] Unable to send Temperature data",
						robotCI.getNetworkAddress());
			} else {
				BatteryStatus status = new BatteryStatus();
				status.setAlarmActive((boolean) getReadings());

				return new BatteryMessage(status, robotCI.getNetworkAddress());
			}
		}
		return null;
	}

	public class ArrayCircularQueue {
		private int pointer = 0;
		private int elementsQnt = 0;
		private int[] queue;

		public ArrayCircularQueue(int maxElements) {
			queue = new int[maxElements];

			for (int i = 0; i < queue.length; i++) {
				queue[i] = -1;
			}
		}

		public void insert(int o) {
			queue[pointer] = o;
			pointer = (pointer + 1) % queue.length;
			elementsQnt++;
		}

		public boolean isEmpty() {
			int position = (pointer - 1) % queue.length;
			if (position < 0)
				position += queue.length;

			return queue[position] == -1;
		}

		public Object remove() {
			if (isEmpty()) {
				return null;
			}

			int position1 = (pointer - 1) % queue.length;
			if (position1 < 0)
				position1 += queue.length;
			Object toReturn = queue[position1];
			queue[position1] = -1;

			pointer--;
			elementsQnt--;

			int position2 = (pointer) % queue.length;
			if (position2 < 0)
				position2 += queue.length;
			pointer = position2;

			return toReturn;
		}

		public int[] getQueue() {
			return queue;
		}

		public int peekObject() {
			int position = (pointer - 1) % queue.length;
			if (position < 0)
				position += queue.length;
			return queue[position];
		}

		public void printQueue(Object[] queue) {
			int i = 0;
			for (Object obj : queue) {
				System.out.println("OBJ" + (i++) + obj);
			}
		}

		public void printQueue() {
			System.out.println("################");
			System.out.println("Pointer: " + pointer);
			int i = 0;
			for (Object obj : queue) {
				if (i == pointer) {
					System.out.print(">");
				}
				System.out.println("OBJ[" + (i++) + "]: " + obj);
			}
		}

		public double calculateMean() {
			int sum = 0;
			for (int i = 0; i < queue.length; i++) {
				if (queue[i] >= 0) {
					sum += queue[i];
				}
			}
			return sum / elementsQnt;
		}
	}
}
