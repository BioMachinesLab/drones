import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.pi4j.system.SystemInfo;

public class OneWireTemperatureTester extends Thread {
	private static final int ONEWIRE_DEVICE_UPDATE_DELAY = 100;
	private static final String PATH = "/sys/bus/w1/devices";
	private static final String FILE = "/w1_slave";

	private static final String SENSOR_NAME_START = "10-";

	private ArrayList<String> devices;

	private boolean available = false;
	private double temperatures[];

	public OneWireTemperatureTester() {
		configure();
	}

	private void configure() {
		devices = new ArrayList<String>();
		File folder = new File(PATH);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isDirectory()
					&& file.getName().startsWith(SENSOR_NAME_START)) {
				devices.add(file.getAbsolutePath() + FILE);
				System.out.println("Detected sensor: " + file.getAbsolutePath()
						+ FILE);
			}
		}

		if (!devices.isEmpty()) {
			available = true;
			temperatures = new double[devices.size() + 1];
		}

	}

	public boolean isAvailable() {
		return available;
	}

	public Object getReadings() {
		return temperatures;
	}

	private double[] readTemperature() {
		if (available) {
			double[] temps = new double[devices.size() + 1];

			try {
				temps[0] = SystemInfo.getCpuTemperature();
			} catch (IOException | InterruptedException e) {
				System.err
						.println("[OneWireTemperatureModuleInput] Unable to fetch system temperature "
								+ e.getMessage());
			}

			for (int i = 0; i < devices.size(); i++) {
				temps[i + 1] = getSensorTemperature(devices.get(i));
			}

			return temps;
		} else {
			return new double[] {};
		}
	}

	private double getSensorTemperature(String path) {
		double temperature = -1;

		BufferedReader reader = null;
		boolean checkSumCorrect = false;
		try {
			reader = new BufferedReader(new FileReader(path));

			String str = reader.readLine();
			int index = -1;
			while (str != null) {
				if (str.contains("YES")) {
					checkSumCorrect = true;
				}

				index = str.indexOf("t=");
				if (index >= 0) {
					break;
				}
				str = reader.readLine();
			}
			if (index < 0) {
				System.err
						.println("[OneWireTemperatureModuleInput] Unable to read sensor "
								+ path);
			}
			temperature = Integer.parseInt(str.substring(index + 2)) / 1000.0;
		} catch (IOException e) {
			System.err
					.println("[OneWireTemperatureModuleInput] Error opening BufferedReader for sensor "
							+ path);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.err
							.println("[OneWireTemperatureModuleInput] Error closing BufferedReader for sensor "
									+ path);
				}
			}
		}

		return checkSumCorrect ? temperature : -1;
	}

	@Override
	public void run() {
		try {
			while (true) {
				temperatures = readTemperature();

				Thread.sleep(ONEWIRE_DEVICE_UPDATE_DELAY);
			}

		} catch (InterruptedException e) {
			System.out.println("[OneWireTemperatureModuleInput] Terminated!");
		}
	}

	public static void main(String[] args) {
		OneWireTemperatureTester module = new OneWireTemperatureTester();
		module.start();

		while (true) {
			try {
				double[] temps = (double[]) module.getReadings();
				
				for(double temp:temps){
					System.out.println("Temperature: "+temp);
				}
				
				System.out.println("############");
				sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
