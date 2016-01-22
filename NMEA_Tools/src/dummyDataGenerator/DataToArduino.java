package dummyDataGenerator;

import java.util.ArrayList;

public class DataToArduino {
	private static final String NMEA_SENTENCES_FILE = "data/nmeaRawData.txt";
	private ArrayList<String> nmeaSentences;
	private int index;

	public DataToArduino() {
		nmeaSentences = new FileParser()
				.plainTextFileParser(NMEA_SENTENCES_FILE);
		index = 0;
	}

	public String getSentence() {
		if (index >= nmeaSentences.size())
			index = 0;
		return nmeaSentences.get(index++);
	}

}
