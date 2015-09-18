package commoninterface.utils.jcoord;

import java.io.Serializable;
import net.jafama.FastMath;

/**
 * Class to represent an Ordnance Survey grid reference
 * 
 * (c) 2006 Jonathan Stott
 * 
 * Created on 11-02-2006
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */
public class OSRef implements Serializable {

  /**
   * Easting
   */
  private double easting;

  /**
   * Northing
   */
  private double northing;


  /**
   * Create a new Ordnance Survey grid reference.
   * 
   * @param easting
   *          the easting in metres
   * @param northing
   *          the northing in metres
   * @since 1.0
   */
  public OSRef(double easting, double northing) {
    this.easting = easting;
    this.northing = northing;
  }


  /**
   * Take a string formatted as a six-figure OS grid reference (e.g. "TG514131")
   * and create a new OSRef object that represents that grid reference. The
   * first character must be H, N, S, O or T. The second character can be any
   * uppercase character from A through Z excluding I.
   * 
   * @param ref
   *          a String representing a six-figure Ordnance Survey grid reference
   *          in the form XY123456
   * @throws IllegalArgumentException
   *           if ref is not of the form XY123456
   * @since 1.0
   */
  public OSRef(String ref) throws IllegalArgumentException {
    // if (ref.matches(""))
    char char1 = ref.charAt(0);
    char char2 = ref.charAt(1);
    // Thanks to Nick Holloway for pointing out the radix bug here
    int east = Integer.parseInt(ref.substring(2, 5)) * 100;
    int north = Integer.parseInt(ref.substring(5, 8)) * 100;
    if (char1 == 'H') {
      north += 1000000;
    } else if (char1 == 'N') {
      north += 500000;
    } else if (char1 == 'O') {
      north += 500000;
      east += 500000;
    } else if (char1 == 'T') {
      east += 500000;
    }
    int char2ord = char2;
    if (char2ord > 73)
      char2ord--; // Adjust for no I
    double nx = ((char2ord - 65) % 5) * 100000;
    double ny = (4 - FastMath.floor((char2ord - 65) / 5)) * 100000;
    easting = east + nx;
    northing = north + ny;
  }


  /**
   * Return a String representation of this OSGB grid reference showing the
   * easting and northing.
   * 
   * @return a String represenation of this OSGB grid reference
   * @since 1.0
   */
  public String toString() {
    return "(" + easting + ", " + northing + ")";
  }


  /**
   * Return a String representation of this OSGB grid reference using the
   * six-figure notation in the form XY123456
   * 
   * @return a String representing this OSGB grid reference in six-figure
   *         notation
   * @since 1.0
   */
  public String toSixFigureString() {
    int hundredkmE = (int) FastMath.floor(easting / 100000);
    int hundredkmN = (int) FastMath.floor(northing / 100000);
    String firstLetter;
    if (hundredkmN < 5) {
      if (hundredkmE < 5) {
        firstLetter = "S";
      } else {
        firstLetter = "T";
      }
    } else if (hundredkmN < 10) {
      if (hundredkmE < 5) {
        firstLetter = "N";
      } else {
        firstLetter = "O";
      }
    } else {
      firstLetter = "H";
    }

    int index = 65 + ((4 - (hundredkmN % 5)) * 5) + (hundredkmE % 5);
    // int ti = index;
    if (index >= 73)
      index++;
    String secondLetter = Character.toString((char) index);

    int e = (int) FastMath.floor((easting - (100000 * hundredkmE)) / 100);
    int n = (int) FastMath.floor((northing - (100000 * hundredkmN)) / 100);
    String es = "" + e;
    if (e < 100)
      es = "0" + es;
    if (e < 10)
      es = "0" + es;
    String ns = "" + n;
    if (n < 100)
      ns = "0" + ns;
    if (n < 10)
      ns = "0" + ns;

    return firstLetter + secondLetter + es + ns;
  }


  /**
   * Convert this OSGB grid reference to a latitude/longitude pair using the
   * OSGB36 datum. Note that, the LatLng object may need to be converted to the
   * WGS84 datum depending on the application.
   * 
   * @return a LatLng object representing this OSGB grid reference using the
   *         OSGB36 datum
   * @since 1.0
   */
  public LatLon toLatLng() {
    double OSGB_F0 = 0.9996012717;
    double N0 = -100000.0;
    double E0 = 400000.0;
    double phi0 = FastMath.toRadians(49.0);
    double lambda0 = FastMath.toRadians(-2.0);
    double a = RefEll.AIRY_1830.getMaj();
    double b = RefEll.AIRY_1830.getMin();
    double eSquared = RefEll.AIRY_1830.getEcc();
    double phi = 0.0;
    double lambda = 0.0;
    double E = this.easting;
    double N = this.northing;
    double n = (a - b) / (a + b);
    double M = 0.0;
    double phiPrime = ((N - N0) / (a * OSGB_F0)) + phi0;
    do {
      M =
          (b * OSGB_F0)
              * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n)) * (phiPrime - phi0))
                  - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                      * FastMath.sin(phiPrime - phi0) * FastMath.cos(phiPrime + phi0))
                  + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                      * FastMath.sin(2.0 * (phiPrime - phi0)) * FastMath
                      .cos(2.0 * (phiPrime + phi0))) - (((35.0 / 24.0) * n * n * n)
                  * FastMath.sin(3.0 * (phiPrime - phi0)) * FastMath
                  .cos(3.0 * (phiPrime + phi0))));
      phiPrime += (N - N0 - M) / (a * OSGB_F0);
    } while ((N - N0 - M) >= 0.001);
    double v =
        a * OSGB_F0
            * FastMath.pow(1.0 - eSquared * Util.sinSquared(phiPrime), -0.5);
    double rho =
        a * OSGB_F0 * (1.0 - eSquared)
            * FastMath.pow(1.0 - eSquared * Util.sinSquared(phiPrime), -1.5);
    double etaSquared = (v / rho) - 1.0;
    double VII = FastMath.tan(phiPrime) / (2 * rho * v);
    double VIII =
        (FastMath.tan(phiPrime) / (24.0 * rho * FastMath.pow(v, 3.0)))
            * (5.0 + (3.0 * Util.tanSquared(phiPrime)) + etaSquared - (9.0 * Util
                .tanSquared(phiPrime) * etaSquared));
    double IX =
        (FastMath.tan(phiPrime) / (720.0 * rho * FastMath.pow(v, 5.0)))
            * (61.0 + (90.0 * Util.tanSquared(phiPrime)) + (45.0 * Util
                .tanSquared(phiPrime) * Util.tanSquared(phiPrime)));
    double X = Util.sec(phiPrime) / v;
    double XI =
        (Util.sec(phiPrime) / (6.0 * v * v * v))
            * ((v / rho) + (2 * Util.tanSquared(phiPrime)));
    double XII =
        (Util.sec(phiPrime) / (120.0 * FastMath.pow(v, 5.0)))
            * (5.0 + (28.0 * Util.tanSquared(phiPrime)) + (24.0 * Util
                .tanSquared(phiPrime) * Util.tanSquared(phiPrime)));
    double XIIA =
        (Util.sec(phiPrime) / (5040.0 * FastMath.pow(v, 7.0)))
            * (61.0
                + (662.0 * Util.tanSquared(phiPrime))
                + (1320.0 * Util.tanSquared(phiPrime) * Util
                    .tanSquared(phiPrime)) + (720.0 * Util.tanSquared(phiPrime)
                * Util.tanSquared(phiPrime) * Util.tanSquared(phiPrime)));
    phi =
        phiPrime - (VII * FastMath.pow(E - E0, 2.0))
            + (VIII * FastMath.pow(E - E0, 4.0)) - (IX * FastMath.pow(E - E0, 6.0));
    lambda =
        lambda0 + (X * (E - E0)) - (XI * FastMath.pow(E - E0, 3.0))
            + (XII * FastMath.pow(E - E0, 5.0)) - (XIIA * FastMath.pow(E - E0, 7.0));

    return new LatLon(FastMath.toDegrees(phi), FastMath.toDegrees(lambda));
  }


  /**
   * Get the easting.
   * 
   * @return the easting in metres
   * @since 1.0
   */
  public double getEasting() {
    return easting;
  }


  /**
   * Get the northing.
   * 
   * @return the northing in metres
   * @since 1.0
   */
  public double getNorthing() {
    return northing;
  }
}
