package main;
import gui.CombinedGui;


public class VMainAquatic {
	
	public static void main(String[] args) {
		try {
			new CombinedGui(new String[]{"--gui","classname=CIResultViewerGui,renderer=(classname=CITwoDRenderer))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
