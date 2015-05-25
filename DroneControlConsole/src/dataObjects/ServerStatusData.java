package dataObjects;

import java.util.ArrayList;

public class ServerStatusData {
	private ArrayList<String> availableBehaviors;
	private ArrayList<String> availableControllers;

	private int connectedClientsQty = 0;

	// Setters
	public void setAvailableBehaviors(ArrayList<String> availableBehaviors) {
		this.availableBehaviors = availableBehaviors;
	}

	public void setAvailableControllers(ArrayList<String> availableControllers) {
		this.availableControllers = availableControllers;
	}

	public void setConnectedClientsQty(int connectedClientsQty) {
		this.connectedClientsQty = connectedClientsQty;
	}

	// Getters
	public ArrayList<String> getAvailableBehaviors() {
		return availableBehaviors;
	}

	public ArrayList<String> getAvailableControllers() {
		return availableControllers;
	}

	public int getConnectedClientsQty() {
		return connectedClientsQty;
	}
}
