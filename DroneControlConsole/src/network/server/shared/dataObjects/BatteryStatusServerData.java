package network.server.shared.dataObjects;

public class BatteryStatusServerData {
	private int batteryID;
	private double[] cellsVoltages;
	private double batteryTemperature;

	public BatteryStatusServerData() {
		batteryTemperature = -1;
		batteryID = -1;
	}

	public BatteryStatusServerData(int batteryID, double[] cellsVoltages,
			double batteryTemperature) {
		this.batteryID = batteryID;
		this.cellsVoltages = cellsVoltages;
		this.batteryTemperature = batteryTemperature;
	}

	// Getters
	public int getCellCount() {
		if (cellsVoltages != null) {
			return cellsVoltages.length;
		} else {
			return -1;
		}
	}

	public int getBatteryID() {
		return batteryID;
	}

	public double getBatteryTemperature() {
		return batteryTemperature;
	}

	public double[] getCellsVoltages() {
		return cellsVoltages;
	}

	// Setters
	public void setBatteryID(int batteryID) {
		this.batteryID = batteryID;
	}

	public void setBatteryTemperature(double batteryTemperature) {
		this.batteryTemperature = batteryTemperature;
	}

	public void setCellsVoltage(double[] cellsVoltage) {
		this.cellsVoltages = cellsVoltage;
	}
}
