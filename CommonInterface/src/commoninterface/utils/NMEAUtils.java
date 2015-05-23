package commoninterface.utils;

import java.util.Scanner;

/**
 * Class for checksum calculation and check on NMEA sentences and PMTK commands
 * 
 * @author VCosta
 */
public class NMEAUtils {
	/**
	 * Main function to call the desired function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// new NMEAUtils().calculateNMEAChecksum();
		new NMEAUtils().checkNMEAChecksum();
	}

	/**
	 * Calculates a NMEA (or PMTK) sentence checksum (basically a XOR between
	 * all members in the NMEA/PMTK string (excluding the $ character). The
	 * interactions is made with a dialog and using the System.in
	 */
	public void calculateNMEAChecksum() {
		Scanner in = new Scanner(System.in);
		
		while(true) {
			System.out.println("Entrer NMEA sentence to calculate checksum: ");
			String checksum = calculateNMEAChecksum(in.nextLine());
			// System.out.println("Checksum (DEC): " + checksum);
			System.out.println("Chechsum (HEX): "
					+ checksum);
		}
	}

	/**
	 * Calculates a NMEA (or PMTK) sentence checksum (basically a XOR between
	 * all members in the given NMEA/PMTK string (excluding the $ character)
	 * 
	 * @param sentence
	 *            to calculate checksum
	 * @return the checksum
	 */
	public String calculateNMEAChecksum(String sentence) {
		int checksum = 0;

		for (int i = 0; i < sentence.length(); i++) {
			if (sentence.charAt(i) != '$') {
				if (sentence.charAt(i) == '*')
					break;
				else
					checksum ^= (byte) sentence.charAt(i);
			}
		}
		
		String stringChecksum = Integer.toHexString(checksum).toUpperCase();
		if(stringChecksum.length() == 1)
			stringChecksum = "0"+stringChecksum;

		return stringChecksum;
	}

	/**
	 * Checks if a checksum presented on a NMEA (or PMTK) sentence is correct.
	 * The interactions is made with a dialog and using the System.in
	 */
	public void checkNMEAChecksum() {
		Scanner in = new Scanner(System.in);
		
		while(true) {
			System.out.println("Entrer NMEA sentence to check checksum: ");
			String sentence = in.nextLine();
			boolean check = checkNMEAChecksum(sentence);
			String ck = calculateNMEAChecksum(sentence);
	
			System.out.println("The checksum is "
					+ (check ? "correct" : "incorrect"));
			System.out.println("Expected="+ck);
		}
	}

	/**
	 * Checks if the given NMEA (or PMTK) sentence has a correct checksum
	 * 
	 * @param sentence
	 *            to test
	 * @return the result of the test (true if the checksum is correct and false
	 *         if not)
	 */
	public boolean checkNMEAChecksum(String sentence) {
		
		if(sentence.length() < 4)
			return false;
		
		if (sentence.charAt(sentence.length() - 3) == '*') {
			
			String ck = sentence.substring(sentence.length() - 2,sentence.length());
			String calculatedChecksum = calculateNMEAChecksum(sentence);
			return ck.equals(calculatedChecksum);
		}
		
		return false;
	}
}