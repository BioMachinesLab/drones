package input;

import java.io.IOException;
import java.text.ParseException;

import dataObjects.SystemInformationsData;

public class SystemInformationsInput implements ControllerInput {

	@Override
	public Object getReadings() {
		try {
			return new SystemInformationsData();
		} catch (IOException | InterruptedException | ParseException e) {
			return null;
		}
	}

}
