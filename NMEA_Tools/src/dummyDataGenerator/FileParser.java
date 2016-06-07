package dummyDataGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileParser {
	public ArrayList<String> plainTextFileParser(String fileName) {
		ArrayList<String> data = new ArrayList<>();
		if (fileName != null) {
			BufferedReader in = null;
			try {
				String sCurrentLine;
				in = new BufferedReader(new FileReader(fileName));
				while ((sCurrentLine = in.readLine()) != null) {
					if (!sCurrentLine.contains("#") && !sCurrentLine.isEmpty()) {
						data.add(sCurrentLine);
					}
				}
			} catch (IOException e) {
				System.err.println("File \"" + fileName + "\" Not Found");
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException ex) {
					System.err.println("Error closing \"" + fileName + "\"");
				}
			}
		}
		return data;
	}

	public ArrayList<String[]> cvsFileParser(String fileName, String separator) {
		ArrayList<String[]> data = new ArrayList<>();
		if (fileName != null) {
			BufferedReader in = null;
			try {
				String sCurrentLine;
				in = new BufferedReader(new FileReader(fileName));
				sCurrentLine = in.readLine();

				while ((sCurrentLine = in.readLine()) != null) {
					if (!sCurrentLine.isEmpty()) {
						data.add(sCurrentLine.split(separator));
					}
				}
			} catch (IOException e) {
				System.err.println("File \"" + fileName + "\" Not Found");
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException ex) {
					System.err.println("Error closing \"" + fileName + "\"");
				}
			}
		}
		return data;
	}

	public ArrayList<String> plainTextFileParserWithEscape(String fileName) {
		ArrayList<String> data = new ArrayList<>();
		if (fileName != null) {
			BufferedReader in = null;
			try {
				String sCurrentLine;
				in = new BufferedReader(new FileReader(fileName));
				while ((sCurrentLine = in.readLine()) != null) {
					if (!sCurrentLine.contains("#") && !sCurrentLine.isEmpty()
							&& !sCurrentLine.contains("'")) {
						data.add(sCurrentLine);
					}
				}
			} catch (IOException e) {
				System.err.println("File \"" + fileName + "\" Not Found");
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException ex) {
					System.err.println("Error closing \"" + fileName + "\"");
				}
			}
		}
		return data;
	}
}