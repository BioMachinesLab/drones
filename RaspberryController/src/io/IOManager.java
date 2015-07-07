package io;

import io.input.BatteryAlarmInput;
import io.input.ControllerInput;
import io.input.FakeGPSModuleInput;
import io.input.FileGPSModuleInput;
import io.input.GPSModuleInput;
import io.input.I2CBatteryModuleInput;
import io.input.I2CCompassLSM303Input;
import io.input.OneWireTemperatureModuleInput;
import io.output.ControllerOutput;
import io.output.DebugLedsOutput;
import io.output.ReversableESCManagerOutput;

import java.io.IOException;
import java.util.ArrayList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import commoninterface.dataobjects.MotorSpeeds;
import commoninterface.network.messages.MotorMessage;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class IOManager {

	private ArrayList<ControllerOutput> outputs = new ArrayList<ControllerOutput>();
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();

	// Inputs
	private GPSModuleInput gpsModule;
	private I2CCompassLSM303Input compassModule;
	private I2CBatteryModuleInput batteryManager;
	private OneWireTemperatureModuleInput temperatureModule;
	private BatteryAlarmInput batteryAlarmInput;

	// Outputs
	private ReversableESCManagerOutput escManager;
	private DebugLedsOutput debugLeds;
	// private BuzzerOutput buzzer;

	// Hardware Instances
	private GpioController gpioController;
	// TODO Refactor I2C initialization!
	private I2CBus i2cBus;

	private String initMessages = "\n";
	private RealAquaticDroneCI drone;
	private MotorSpeeds motorSpeeds;

	public IOManager(RealAquaticDroneCI drone, CIArguments args) {
		this.drone = drone;
		motorSpeeds = new MotorSpeeds();

		initHardwareCommunicatonProtocols(args);
		initOutputs(args);
		initInputs(args);
	}

	public GPSModuleInput getGpsModule() {
		return gpsModule;
	}

	public I2CCompassLSM303Input getCompassModule() {
		return compassModule;
	}

	/**
	 * Initialize the Hardware protocols shared and used on input and output
	 * devices ("physical layer"), like I2C and GPIO management instances
	 */
	private void initHardwareCommunicatonProtocols(CIArguments args) {
		if (args.getFlagIsTrue("i2c")) {
			try {
				// Get I2C instance
				i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
				initMessages += ("\n[INIT] I2C Interface: ok!\n");
			} catch (Exception e) {
				initMessages += "\n[INIT] I2C Interface: not ok!\n";
				e.printStackTrace();
			}
		}

		if (args.getFlagIsTrue("gpio")) {
			try {
				gpioController = GpioFactory.getInstance();
				initMessages += ("\n[INIT] GPIO Controller: ok!\n");
			} catch (Exception | Error e) {
				initMessages += ("\n[INIT] GPIO Controller: not ok! ("
						+ e.getMessage() + ")\n");
			}
		}
	}

	private void initInputs(CIArguments args) {
		// I2C Inputs
		if (args.getFlagIsTrue("i2c")) {
			if (args.getFlagIsTrue("compass")) {
				// Compass Module Init
				compassModule = new I2CCompassLSM303Input(i2cBus, drone);
				initMessages += "[INIT] I2CCompassLSM303Input: "
						+ (compassModule.isAvailable() ? "ok" : "not ok!")
						+ "\n";

				if (compassModule.isAvailable()) {
					compassModule.start();
					inputs.add(compassModule);
				}

			}

			if (args.getFlagIsTrue("battMngr")) {
				// Battery Module Init
				batteryManager = new I2CBatteryModuleInput(i2cBus, drone);
				initMessages += "[INIT] BatteryManager: "
						+ (batteryManager.isAvailable() ? "ok" : "not ok!")
						+ "\n";
				if (batteryManager.isAvailable()) {
					batteryManager.start();
					inputs.add(batteryManager);
				}
			}
		}

		// GPIO Inputs
		if (args.getFlagIsTrue("gpio")) {
			if (args.getFlagIsTrue("battAlarm")) {
				// Battery Alarm Module Init
				batteryAlarmInput = new BatteryAlarmInput(gpioController, drone);
				initMessages += "[INIT] BatteryAlarmInput: "
						+ (batteryAlarmInput.isAvailable() ? "ok" : "not ok!")
						+ "\n";

				if (batteryAlarmInput.isAvailable()) {
					batteryAlarmInput.start();
					inputs.add(batteryAlarmInput);
				}
			}
		}

		// Misc Inputs
		if (args.getFlagIsTrue("temperature")) {
			temperatureModule = new OneWireTemperatureModuleInput(drone);
			initMessages += "[INIT] OneWireTemperature: "
					+ (temperatureModule.isAvailable() ? "ok" : "not ok!")
					+ "\n";

			if (temperatureModule.isAvailable()) {
				temperatureModule.start();
				inputs.add(temperatureModule);
			}
		}

		// GPS Inputs
		if (args.getFlagIsTrue("gps")) {
			try {
				// GPS Module Init
				gpsModule = new GPSModuleInput(drone);
				initMessages += "[INIT] GPSModule: "
						+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";

				if (gpsModule.isAvailable()) {
					gpsModule.enableLocalLog();
					inputs.add(gpsModule);
				}
			} catch (Exception e) {
				initMessages += "[INIT] GPSModule: not ok! (" + e.getMessage()
						+ ")\n";
			}
		} else if (args.getFlagIsTrue("filegps")) {
			try {
				// GPS Module Init
				gpsModule = new FileGPSModuleInput();
				initMessages += "[INIT] FileGPSModule: "
						+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";

				if (gpsModule.isAvailable()) {
					inputs.add(gpsModule);
				}
			} catch (Exception e) {
				initMessages += "[INIT] FileGPSModule: not ok! ("
						+ e.getMessage() + ")\n";
			}
		} else if (args.getFlagIsTrue("fakegps")) {
			try {
				// GPS Module Init
				gpsModule = new FakeGPSModuleInput(drone);
				initMessages += "[INIT] FakeGPSModule: "
						+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";

				if (gpsModule.isAvailable()) {
					inputs.add(gpsModule);
				}
			} catch (Exception e) {
				initMessages += "[INIT] FakeGPSModule: not ok! ("
						+ e.getMessage() + ")\n";
			}
		}
	}

	private void initOutputs(CIArguments args) {
		if (args.getFlagIsTrue("esc")) {
			try {
				// ESC Output Init
				escManager = new ReversableESCManagerOutput(motorSpeeds,
						gpioController);
				initMessages += "[INIT] ESCManager: "
						+ (escManager.isAvailable() ? "ok" : "not ok!") + "\n";

				if (escManager.isAvailable()) {
					escManager.start();
					outputs.add(escManager);
				}
			} catch (Exception e) {
				initMessages += "[INIT] ESCManager: not ok! (" + e.getMessage()
						+ ")\n";
			}
		}

		if (args.getFlagIsTrue("gpio")) {
			if (args.getFlagIsTrue("leds")) {
				try {
					// Debug Leds Init
					debugLeds = new DebugLedsOutput(gpioController);
					initMessages += "[INIT] DebugLEDs: "
							+ (debugLeds.isAvailable() ? "ok" : "not ok!")
							+ "\n";

					if (debugLeds.isAvailable()) {
						debugLeds.start();
						outputs.add(debugLeds);
						debugLeds.addBlinkLed(0);
					}
				} catch (Exception e) {
					initMessages += "[INIT] DebugLEDs: not ok! ("
							+ e.getMessage() + ")\n";
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
	}

	public void shutdown() {
		System.out.println("# Shutting down IO...");
		if (escManager != null)
			shutdownMotors();

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
			motorSpeeds.setSpeeds(new MotorMessage(left, right, drone
					.getNetworkAddress()));
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

}
