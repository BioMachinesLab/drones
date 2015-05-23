package io.input;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import commoninterface.network.messages.MessageProvider;

public abstract class I2CInput extends Thread implements ControllerInput,
		MessageProvider {
	protected final static int I2C_DEVICE_UPDATE_DELAY = 15;
	
	protected int deviceAddress;
	protected I2CBus i2cBus;
	protected I2CDevice device;
	protected boolean available = false;
	protected boolean deviceActiveMode = true;

	public I2CInput(I2CBus i2cBus, int deviceAddress) {
		this.i2cBus = i2cBus;
		this.deviceAddress = deviceAddress;
	}

	public void initializeDevice() throws IOException, InterruptedException {
		device = i2cBus.getDevice(deviceAddress);
		Thread.sleep(15);
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	public I2CBus getI2CBus() {
		return i2cBus;
	}

	public I2CDevice getDevice() {
		return device;
	}

	public int getDeviceAddress() {
		return deviceAddress;
	}

	protected int readByte(byte address) throws IOException {
		return device.read(address);
	}
	
	public abstract void standBy();
	public abstract void wakeUp();
}
