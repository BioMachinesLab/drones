package generalTools;

import java.util.Scanner;

/**
 * Class for checksum calculation and check on NMEA sentences and PMTK commands
 * 
 * @author Vasco Craveiro Costa
 * @license Under CC BY-SA 4.0 License
 * 
 */
public class Checksum_Tools {
	/**
	 * Main function to call the desired function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new Checksum_Tools().calculateNMEAChecksum();
		//new Checksum_Tools().checkNMEAChecksum();
	}

	/**
	 * Calculates a NMEA (or PMTK) sentence checksum (basically a XOR between
	 * all members in the NMEA/PMTK string (excluding the $ character). The
	 * interactions is made with a dialog and using the System.in
	 */
	public void calculateNMEAChecksum() {
		Scanner in = new Scanner(System.in);

		System.out.println("Entrer NMEA sentence to calculate checksum: ");
		int checksum = calculateNMEAChecksum(in.nextLine());
		in.close();

		// System.out.println("Checksum (DEC): " + checksum);
		System.out.println("Chechsum (HEX): "
				+ Integer.toHexString(checksum).toUpperCase());
	}

	/**
	 * Calculates a NMEA (or PMTK) sentence checksum (basically a XOR between
	 * all members in the given NMEA/PMTK string (excluding the $ character)
	 * 
	 * @param sentence
	 *            to calculate checksum
	 * @return the checksum
	 */
	public int calculateNMEAChecksum(String sentence) {
		int checksum = 0;

		for (int i = 0; i < sentence.length(); i++) {
			if (sentence.charAt(i) != '$'){
				if (sentence.charAt(i) == '*')
					break;
				else
					checksum ^= (byte) sentence.charAt(i);
			}
		}

		return checksum;
	}

	/**
	 * Checks if a checksum presented on a NMEA (or PMTK) sentence is correct.
	 * The interactions is made with a dialog and using the System.in
	 */
	public void checkNMEAChecksum() {
		Scanner in = new Scanner(System.in);

		System.out.println("Entrer NMEA sentence to check checksum: ");
		String sentence = in.nextLine();
		boolean check = checkNMEAChecksum(sentence);
		in.close();

		System.out.println("The checksum is "
				+ (check ? "correct" : "incorrect"));
	}

	/**
	 * Checks if the given NMEA (or PMTK) sentence has a correct checksum
	 * 
	 * @param sentence
	 *            to test
	 * @return the result of the test (true if the checksum is correct and false
	 *         if not)
	 */
	private boolean checkNMEAChecksum(String sentence) {
		int checksum = calculateNMEAChecksum(sentence);

		if (sentence.charAt(sentence.length() - 3) == '*') {
			String ck = sentence.substring(sentence.length() - 2,
					sentence.length());
			if (ck.equals(Integer.toHexString(checksum).toUpperCase()))
				return true;
			else
				return false;
		}
		return false;
	}
}