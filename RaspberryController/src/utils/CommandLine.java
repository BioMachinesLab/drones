package utils;

import java.io.IOException;

public class CommandLine {

	public static void executeShellCommand(String pCommand) {
		try {
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(pCommand);
			pr.waitFor();
		} catch (IOException | InterruptedException e) {
			System.out.println("Something went wrong when executing command in the commnad line");
			e.printStackTrace();
		}
	}
	
}
