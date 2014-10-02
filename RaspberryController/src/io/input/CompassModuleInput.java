package io.input;

public class CompassModuleInput implements ControllerInput {
	
	private boolean available = false;
	
	public CompassModuleInput() {
		available = false;
	}

	@Override
	public Object getReadings() {
		return null;
	}
	
	@Override
	public boolean isAvailable() {
		return available;
	}

}
