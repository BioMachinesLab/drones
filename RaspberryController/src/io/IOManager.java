package io;

import io.input.ControllerInput;
import io.input.GPSModuleInput;
import io.input.I2CBatteryManagerInput;
import io.input.I2CCompassModuleInput;
import io.output.ControllerOutput;
import io.output.DebugLedsOutput;
import io.output.ReversableESCManagerOutputV2;

import java.io.IOException;
import java.util.ArrayList;

import network.messages.MotorMessage;
import main.Controller;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;

import dataObjects.MotorSpeeds;

public class IOManager {
	
	private ArrayList<ControllerOutput> outputs = new ArrayList<ControllerOutput>();
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();

	//Inputs
	private GPSModuleInput gpsModule;
	private I2CCompassModuleInput compassModule;
	private I2CBatteryManagerInput batteryManager;
	
	//Outputs
	private ReversableESCManagerOutputV2 escManager;
	private DebugLedsOutput debugLeds;
	
	// Hardware Instances
	private GpioController gpioController;
	//TODO Refactor I2C initialization!
	private I2CBus i2cBus;
	
	private String initMessages = "\n";
	private Controller controller;
	private MotorSpeeds motorSpeeds;
		
	public IOManager(Controller controller) {
		this.controller  = controller;
		motorSpeeds = new MotorSpeeds();
		initHardwareCommunicatonProtocols();
		initInputs();
		initOutputs();
		addShutdownHooks();
		
	}
	
	/**
	 * Initialize the Hardware protocols shared and used on input and output
	 * devices ("physical layer"), like I2C and GPIO management instances
	 */
	private void initHardwareCommunicatonProtocols() {
		// try {
		// // Get I2C instance
		// i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
		// } catch (IOException e) {
		// initMessages += "\n[INIT] I2C Interface: not ok!\n";
		// e.printStackTrace();
		// }

		try {
			gpioController = GpioFactory.getInstance();
		} catch (Exception | Error e) {
			initMessages += ("\n[INIT] GPIO Controller: not ok! ("
					+ e.getMessage() + ")\n");
		}
	}
	
	private void initInputs() {
		// Compass Module Init
		// compassModule = new I2CCompassModuleInput(i2cBus);
		// initMessages += "[INIT] I2CCompassModule: "
		// + (compassModule.isAvailable() ? "ok" : "not ok!") + "\n";
		//
		// if (compassModule.isAvailable()) {
		// compassModule.start();
		// inputs.add(compassModule);
		// System.out.print(".");
		// }

		// Battery Module Init
		// batteryManager = new I2CBatteryManagerInput();
		// initMessages += "[INIT] BatteryManager: "
		// + (batteryManager.isAvailable() ? "ok" : "not ok!") + "\n";
		// if (batteryManager.isAvailable()) {
		// batteryManager.start();
		// inputs.add(batteryManager);
		// System.out.print(".");
		// }
		
		try {
			// GPS Module Init
			gpsModule = new GPSModuleInput();
			initMessages += "[INIT] GPSModule: "
					+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";
	
			if (gpsModule.isAvailable()) {
				gpsModule.enableLocalLog();
				inputs.add(gpsModule);
				System.out.print(".");
			}
		} catch(Exception e) {
			initMessages += "[INIT] GPSModule: not ok! ("
					+ e.getMessage() + ")\n";
		}
	}

	private void initOutputs() {
		
		try {
			// ESC Output Init
			escManager = new ReversableESCManagerOutputV2(motorSpeeds, gpioController);

			initMessages += "[INIT] ESCManager: "
					+ (escManager.isAvailable() ? "ok" : "not ok!") + "\n";
	
			if (escManager.isAvailable()) {
				escManager.start();
				outputs.add(escManager);
				System.out.print(".");
			}
		
		} catch(Exception e) {
			initMessages += "[INIT] ESCManager: not ok! ("
					+ e.getMessage() + ")\n";
		}
		
		try {
			// Debug Leds Init
			debugLeds = new DebugLedsOutput(gpioController);
			initMessages += "[INIT] DebugLEDs: "
					+ (debugLeds.isAvailable() ? "ok" : "not ok!") + "\n";
			if (debugLeds.isAvailable()) {
				debugLeds.start();
				outputs.add(debugLeds);
	
				debugLeds.addBlinkLed(0);
	
				System.out.print(".");
			}
		} catch(Exception e) {
			initMessages += "[INIT] DebugLEDs: not ok! ("
					+ e.getMessage() + ")\n";
		}
	}
	
	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("# Shutting down IO...");
				if (escManager != null)
					setMotorSpeeds(-1, -1);
				
				if (compassModule != null)
					compassModule.interrupt();

				if (i2cBus != null) {
					try {
						i2cBus.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (gpsModule != null)
					gpsModule.closeSerial();

				if (debugLeds != null)
					debugLeds.shutdownLeds();

				if (gpioController != null)
					gpioController.shutdown();
				
				System.out.println("# Finished IO cleanup!");
			}
		});
	}
	
	public String getInitMessages() {
		return initMessages;
	}
	
	public ArrayList<ControllerInput> getInputs() {
		return inputs;
	}
	
	public ArrayList<ControllerOutput> getOutputs() {
		return outputs;
	}
	
	public void setMotorSpeeds(double left, double right) {
		motorSpeeds.setSpeeds(new MotorMessage(left, right));
	}
}