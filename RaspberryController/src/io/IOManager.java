package io;

import io.input.ControllerInput;
import io.input.FakeGPSModuleInput;
import io.input.FileGPSModuleInput;
import io.input.GPSModuleInput;
import io.input.I2CBatteryModuleInput;
import io.input.I2CCompassModuleInput;
//import io.output.BuzzerOutput;
//import io.output.BuzzerOutput.BuzzerMode;
import io.output.ControllerOutput;
import io.output.DebugLedsOutput;
import io.output.ReversableESCManagerOutputV2;
import io.output.ServoOutput;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import network.messages.MotorMessage;
import utils.Logger;
import behaviors.CalibrationCIBehavior;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

import dataObjects.MotorSpeeds;

public class IOManager {

	private final static String CONFIG_FILE = "io_config.conf";

	private ArrayList<ControllerOutput> outputs = new ArrayList<ControllerOutput>();
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();

	// Inputs
	private GPSModuleInput gpsModule;
	private I2CCompassModuleInput compassModule;
	private I2CBatteryModuleInput batteryManager;

	// Outputs
	private ReversableESCManagerOutputV2 escManager;
	private DebugLedsOutput debugLeds;
	// private BuzzerOutput buzzer;

	// Hardware Instances
	private GpioController gpioController;
	// TODO Refactor I2C initialization!
	private I2CBus i2cBus;

	private String initMessages = "\n";
	private RealAquaticDroneCI drone;
	private MotorSpeeds motorSpeeds;

	private LinkedList<String> enabledIO = new LinkedList<String>();

	private Logger fileLogger;

	public IOManager(RealAquaticDroneCI drone) {
		this.drone = drone;
		motorSpeeds = new MotorSpeeds();

		loadConfigurations();

		initHardwareCommunicatonProtocols();
		initOutputs();
		initInputs();

		if (enabledIO.contains("filelogger")) {
			fileLogger = new Logger(drone);
			fileLogger.start();
		}

	}

	private void loadConfigurations() {

		try {
			Scanner s = new Scanner(new File(CONFIG_FILE));
			while (s.hasNextLine()) {
				String line = s.nextLine();

				// So we can put comments on the io_config.conf
				if (line.startsWith("#") || line.length() < 2) {
					continue;
				}

				String[] split = line.split(" ");
				if (split[1].equals("1"))
					enabledIO.add(split[0]);
			}
			s.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public GPSModuleInput getGpsModule() {
		return gpsModule;
	}

	public I2CCompassModuleInput getCompassModule() {
		return compassModule;
	}

	/**
	 * Initialize the Hardware protocols shared and used on input and output
	 * devices ("physical layer"), like I2C and GPIO management instances
	 */
	private void initHardwareCommunicatonProtocols() {

		if (enabledIO.contains("i2c")) {
			try {
				// Get I2C instance
				i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
			} catch (Exception e) {
				initMessages += "\n[INIT] I2C Interface: not ok!\n";
				e.printStackTrace();
			}
		}

		if (enabledIO.contains("gpio")) {
			try {
				gpioController = GpioFactory.getInstance();
			} catch (Exception | Error e) {
				initMessages += ("\n[INIT] GPIO Controller: not ok! ("
						+ e.getMessage() + ")\n");
			}
		}
	}

	private void initInputs() {
		if (enabledIO.contains("compass") && enabledIO.contains("i2c")) {
			// Compass Module Init
			compassModule = new I2CCompassModuleInput(i2cBus);
			initMessages += "[INIT] I2CCompassModule: "
					+ (compassModule.isAvailable() ? "ok" : "not ok!") + "\n";

			if (compassModule.isAvailable()) {
				compassModule.start();
				inputs.add(compassModule);
				System.out.print(".");
			}

			if (enabledIO.contains("autocompasscalibration")) {
				drone.startBehavior(new CalibrationCIBehavior(new CIArguments(
						""), drone));
			}

		}

		if (enabledIO.contains("battery") && enabledIO.contains("i2c")) {
			// Battery Module Init
			batteryManager = new I2CBatteryModuleInput(i2cBus);
			initMessages += "[INIT] BatteryManager: "
					+ (batteryManager.isAvailable() ? "ok" : "not ok!") + "\n";
			if (batteryManager.isAvailable()) {
				batteryManager.start();
				inputs.add(batteryManager);
				System.out.print(".");
			}
		}

		if (enabledIO.contains("gps")) {
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
			} catch (Exception e) {
				initMessages += "[INIT] GPSModule: not ok! (" + e.getMessage()
						+ ")\n";
			}
		} else if (enabledIO.contains("filegps")) {
			try {
				// GPS Module Init
				gpsModule = new FileGPSModuleInput();
				initMessages += "[INIT] FileGPSModule: "
						+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";

				if (gpsModule.isAvailable()) {
					inputs.add(gpsModule);
					System.out.print(".");
				}
			} catch (Exception e) {
				initMessages += "[INIT] FileGPSModule: not ok! ("
						+ e.getMessage() + ")\n";
			}
		} else if (enabledIO.contains("fakegps")) {
			try {
				// GPS Module Init
				gpsModule = new FakeGPSModuleInput(drone);
				initMessages += "[INIT] FakeGPSModule: "
						+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";

				if (gpsModule.isAvailable()) {
					inputs.add(gpsModule);
					System.out.print(".");
				}
			} catch (Exception e) {
				initMessages += "[INIT] FakeGPSModule: not ok! ("
						+ e.getMessage() + ")\n";
			}
		}
	}

	private void initOutputs() {
		if (enabledIO.contains("esc") && enabledIO.contains("i2c")) {
			try {
				// ESC Output Init
				escManager = new ReversableESCManagerOutputV2(motorSpeeds,
						gpioController);

				initMessages += "[INIT] ESCManager: "
						+ (escManager.isAvailable() ? "ok" : "not ok!") + "\n";

				if (escManager.isAvailable()) {
					escManager.start();
					outputs.add(escManager);
					System.out.print(".");
				}

			} catch (Exception e) {
				initMessages += "[INIT] ESCManager: not ok! (" + e.getMessage()
						+ ")\n";
			}
		}

		if (enabledIO.contains("servos") && enabledIO.contains("i2c")) {
			try {
				// ESC Output Init
				escManager = new ServoOutput(motorSpeeds, gpioController);

				initMessages += "[INIT] ServoManager: "
						+ (escManager.isAvailable() ? "ok" : "not ok!") + "\n";

				if (escManager.isAvailable()) {
					escManager.start();
					outputs.add(escManager);
					System.out.print(".");
				}

			} catch (Exception e) {
				initMessages += "[INIT] ServoManager: not ok! ("
						+ e.getMessage() + ")\n";
			}
		}

		if (enabledIO.contains("leds") && enabledIO.contains("i2c")) {
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
			} catch (Exception e) {
				initMessages += "[INIT] DebugLEDs: not ok! (" + e.getMessage()
						+ ")\n";
			}
		}

		// if (enabledIO.contains("buzzer")) {
		// try {
		// // Buzzer Init
		// buzzer = new BuzzerOutput();
		// initMessages += "[INIT] Buzzer: "
		// + (buzzer.isAvailable() ? "ok" : "not ok!") + "\n";
		// if (buzzer.isAvailable()) {
		// buzzer.start();
		// outputs.add(buzzer);
		//
		// buzzer.setValue(BuzzerMode.DOUBLE_BEEP);
		//
		// System.out.print(".");
		// }
		// } catch (Exception e) {
		// initMessages += "[INIT] Buzzer: not ok! (" + e.getMessage()
		// + ")\n";
		// }
		// }
	}

	public void shutdown() {
		System.out.println("# Shutting down IO...");
		if (escManager != null)
			shutdownMotors();

		if (fileLogger != null)
			fileLogger.interrupt();

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

	public String getInitMessages() {
		return initMessages;
	}

	public ArrayList<ControllerInput> getInputs() {
		return inputs;
	}

	public ArrayList<ControllerOutput> getOutputs() {
		return outputs;
	}

	public void shutdownMotors() {
		if (escManager != null)
			escManager.disableMotors();
	}

	public void setMotorSpeeds(double left, double right) {
		if (motorSpeeds != null)
			motorSpeeds.setSpeeds(new MotorMessage(left, right));
	}

	public void setMotorSpeeds(MotorMessage message) {
		if (motorSpeeds != null)
			motorSpeeds.setSpeeds(message);
	}

	public MotorSpeeds getMotorSpeeds() {
		return motorSpeeds;
	}

	public DebugLedsOutput getDebugLeds() {
		return debugLeds;
	}

	// public BuzzerOutput getBuzzer() {
	// return buzzer;
	// }
}