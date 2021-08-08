package pro.identpixels;

public class Settings {
	/**
	 * Convenient central location for the configuration of the size
	 * of square regions to search for and the criteria to allow for a match. 
	 * There is no need to change these values for the assignment.
	 */
	public class Configuration {

		public static final int regionSide = 10;
		public static final double thresholdForPixelMatch = 0.0; // 0 = pixels must match exactly
		public static final double proportionForRegionMatch = 1; // 1 = all pixels must match
		
	}

}
