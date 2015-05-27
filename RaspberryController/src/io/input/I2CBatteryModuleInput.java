package io.input;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import commoninterface.RobotCI;
import commoninterface.dataobjects.BatteryStatus;
import commoninterface.network.messages.BatteryMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.SystemStatusMessage;

public class I2CBatteryModuleInput extends I2CInput {
	private final static int ADDR = 0x20;

	/*
	 * I2C Device variables and settings
	 */
	private final static byte CELL1_VOLTAGE_REG = 0x01;
	private final static byte CELL1_CALIBRATION_REG = 0x11;
	private final static byte TEMPERATURE_REG_H = 0x30;
	private final static byte TEMPERATURE_REG_L = 0x31;
	private final static byte ENABLE_REG = 0x3F;

	private final static byte ENABLE_VALUE = (byte) 0xFF;
	private final static byte DISABLE_VALUE = 0x00;

	public final static int VOLTAGE_MULTIPLIER = 1000;
	public final static int TEMPERATURE_MULTIPLIER = 100;

	/*
	 * Other variables
	 */
	private int cellCount = -1;
	private double[] cellsVoltages;
	private double batteryTemperature = -1;

	private RobotCI robotCI;

	public I2CBatteryModuleInput(I2CBus i2cBus, RobotCI robotCI) {
		super(i2cBus, ADDR);
		try {
			this.robotCI = robotCI;
			initializeDevice();
			cellCount = readByte((byte) 0x00);

			if (cellCount != -1) {
				available = true;
				cellsVoltages = new double[cellCount];
			} else {
				throw new IOException();
			}
		} catch (IOException e) {
			System.out
					.println("[I2CBatteryModuleInput] Error on device initialization");
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out
					.println("[I2CBatteryModuleInput] Error on device initialization (interruped thread)");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.BATTERY)) {
			if (!available) {
				return new SystemStatusMessage(
						"[I2CBatteryModuleInput] Unable to send Compass data",
						robotCI.getNetworkAddress());
			}
			return new BatteryMessage(getReadings(),
					robotCI.getNetworkAddress());
		}
		return null;
	}

	@Override
	public BatteryStatus getReadings() {
		BatteryStatus batteryStatus = new BatteryStatus();
		batteryStatus.setBatteryID(getBatteryID());
		batteryStatus.setBatteryTemperature(batteryTemperature);
		batteryStatus.setCellsVoltage(cellsVoltages);

		return batteryStatus;
	}

	// Default getter and setters
	public int getBatteryID() {
		return ADDR;
	}

	public double getBatteryTemperature() {
		return batteryTemperature;
	}

	public double[] getCellsVoltages() {
		return cellsVoltages;
	}

	// Update values (from hardware)
	private void updateCellsVoltages() {
		if (deviceActiveMode) {
			for (int i = 0; i < cellCount; i++) {
				try {
					double voltage = readCellVoltage(i);
					cellsVoltages[i] = voltage;
				} catch (IOException e) {
					System.out
							.println("[I2CBatteryModuleInput] IOException While Updating Cell Voltage (Cell"
									+ i + ")");
				} catch (InterruptedException e) {
					System.out
							.println("[I2CBatteryModuleInput] Interrupted While Updating Temperature (Cell"
									+ i + ")");
				}

			}
		}
	}

	private void updateBatteryTemperature() {
		if (deviceActiveMode) {
			try {
				batteryTemperature = readBatteryTemperature();
			} catch (IOException e) {
				System.out
						.println("[I2CBatteryModuleInput] IOException While Updating Temperature");
			} catch (InterruptedException e) {
				System.out
						.println("[I2CBatteryModuleInput] Interrupted While Updating Temperature");
			}
		}
	}

	// Low level actions (hardware getters)
	private double readCellVoltage(int cell) throws IOException,
			InterruptedException {
		if (cell < 1 || cell > cellCount) {
			throw new NullPointerException();
		} else {
			int cl, ch; // define the MSB and LSB

			ch = readByte((byte) (CELL1_VOLTAGE_REG + cell)); // y MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start
								// &
								// stop

			cl = readByte((byte) (CELL1_VOLTAGE_REG + cell)); // y LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start
								// &
								// stop

			short cout = (short) ((cl | (ch << 8)) & 0xFFFF); // concatenate
																// the MSB
																// and LSB
			return ((double) cout / VOLTAGE_MULTIPLIER);
		}
	}

	private double readBatteryTemperature() throws IOException,
			InterruptedException {
		int tl, th; // define the MSB and LSB

		th = readByte(TEMPERATURE_REG_H); // temperature MSB register
		Thread.sleep(2);

		tl = readByte(TEMPERATURE_REG_L); // temperature LSB register
		Thread.sleep(2);

		short tout = (short) ((tl | (th << 8)) & 0xFFFF); // concat MSB
															// and LSB

		return ((double) tout / TEMPERATURE_MULTIPLIER);
	}

	@Override
	public void standBy() {
		try {
			device.write(ENABLE_REG, DISABLE_VALUE);
			deviceActiveMode = false;
		} catch (IOException e) {
			System.out
					.println("[I2CBatteryModuleInput] Error on going on standby");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void wakeUp() {
		try {
			device.write(ENABLE_REG, ENABLE_VALUE);
			deviceActiveMode = true;
		} catch (IOException e) {
			System.out.println("[I2CBatteryModuleInput] Error on waking up");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void run() {
		while (true) {
			updateCellsVoltages();
			updateBatteryTemperature();
			try {
				Thread.sleep(I2C_DEVICE_UPDATE_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}