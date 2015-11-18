package commoninterface.utils.jcoord;

import java.io.Serializable;
import net.jafama.FastMath;

/**
 * Class to represent a latitude/longitude pair.
 * 
 * (c) 2006 Jonathan Stott
 * 
 * Created on 11-02-2006
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */
public class LatLon implements Serializable {
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 6626864179053038215L;

/**
   * Latitude in degrees.
   */
  private double lat;

  /**
   * Longitude in degrees.
   */
  private double lng;


  /**
   * Create a new LatLng object to represent a latitude/longitude pair.
   * 
   * @param lat
   *          the latitude in degrees
   * @param lng
   *          the longitude in degrees
   * @since 1.0
   */
  public LatLon(double lat, double lng) {
    this.lat = lat;
    this.lng = lng;
  }


  /**
   * Get a String representation of this LatLng object.
   * 
   * @return a String representation of this LatLng object.
   * @since 1.0
   */
  public String toString() {
    return "(" + this.lat + ", " + this.lng + ")";
  }


  /**
   * Convert this latitude and longitude into an OSGB (Ordnance Survey of Great
   * Britain) grid reference.
   * 
   * @return the converted OSGB grid reference
   * @since 1.0
   */
  public OSRef toOSRef() {
    RefEll airy1830 = new RefEll(6377563.396, 6356256.909);
    double OSGB_F0 = 0.9996012717;
    double N0 = -100000.0;
    double E0 = 400000.0;
    double phi0 = FastMath.toRadians(49.0);
    double lambda0 = FastMath.toRadians(-2.0);
    double a = airy1830.getMaj();
    double b = airy1830.getMin();
    double eSquared = airy1830.getEcc();
    double phi = FastMath.toRadians(getLat());
    double lambda = FastMath.toRadians(getLon());
    double E = 0.0;
    double N = 0.0;
    double n = (a - b) / (a + b);
    double v =
        a * OSGB_F0 * FastMath.pow(1.0 - eSquared * Util.sinSquared(phi), -0.5);
    double rho =
        a * OSGB_F0 * (1.0 - eSquared)
            * FastMath.pow(1.0 - eSquared * Util.sinSquared(phi), -1.5);
    double etaSquared = (v / rho) - 1.0;
    double M =
        (b * OSGB_F0)
            * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n)) * (phi - phi0))
                - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                    * FastMath.sin(phi - phi0) * FastMath.cos(phi + phi0))
                + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                    * FastMath.sin(2.0 * (phi - phi0)) * FastMath
                    .cos(2.0 * (phi + phi0))) - (((35.0 / 24.0) * n * n * n)
                * FastMath.sin(3.0 * (phi - phi0)) * FastMath.cos(3.0 * (phi + phi0))));
    double I = M + N0;
    double II = (v / 2.0) * FastMath.sin(phi) * FastMath.cos(phi);
    double III =
        (v / 24.0) * FastMath.sin(phi) * FastMath.pow(FastMath.cos(phi), 3.0)
            * (5.0 - Util.tanSquared(phi) + (9.0 * etaSquared));
    double IIIA =
        (v / 720.0)
            * FastMath.sin(phi)
            * FastMath.pow(FastMath.cos(phi), 5.0)
            * (61.0 - (58.0 * Util.tanSquared(phi)) + FastMath.pow(FastMath.tan(phi),
                4.0));
    double IV = v * FastMath.cos(phi);
    double V =
        (v / 6.0) * FastMath.pow(FastMath.cos(phi), 3.0)
            * ((v / rho) - Util.tanSquared(phi));
    double VI =
        (v / 120.0)
            * FastMath.pow(FastMath.cos(phi), 5.0)
            * (5.0 - (18.0 * Util.tanSquared(phi))
                + (FastMath.pow(FastMath.tan(phi), 4.0)) + (14 * etaSquared) - (58 * Util
                .tanSquared(phi) * etaSquared));

    N =
        I + (II * FastMath.pow(lambda - lambda0, 2.0))
            + (III * FastMath.pow(lambda - lambda0, 4.0))
            + (IIIA * FastMath.pow(lambda - lambda0, 6.0));
    E =
        E0 + (IV * (lambda - lambda0)) + (V * FastMath.pow(lambda - lambda0, 3.0))
            + (VI * FastMath.pow(lambda - lambda0, 5.0));

    return new OSRef(E, N);
  }


  /**
   * Convert this latitude and longitude to a UTM reference.
   * 
   * @return the converted UTM reference
   * @since 1.0
   */
  public UTMRef toUTMRef() {
    double UTM_F0 = 0.9996;
    double a = RefEll.WGS84.getMaj();
    double eSquared = RefEll.WGS84.getEcc();
    double longitude = this.lng;
    double latitude = this.lat;

    double latitudeRad = latitude * (FastMath.PI / 180.0);
    double longitudeRad = longitude * (FastMath.PI / 180.0);
    int longitudeZone = (int) FastMath.floor((longitude + 180.0) / 6.0) + 1;

    // Special zone for Norway
    if (latitude >= 56.0 && latitude < 64.0 && longitude >= 3.0
        && longitude < 12.0) {
      longitudeZone = 32;
    }

    // Special zones for Svalbard
    if (latitude >= 72.0 && latitude < 84.0) {
      if (longitude >= 0.0 && longitude < 9.0) {
        longitudeZone = 31;
      } else if (longitude >= 9.0 && longitude < 21.0) {
        longitudeZone = 33;
      } else if (longitude >= 21.0 && longitude < 33.0) {
        longitudeZone = 35;
      } else if (longitude >= 33.0 && longitude < 42.0) {
        longitudeZone = 37;
      }
    }

    double longitudeOrigin = (longitudeZone - 1) * 6 - 180 + 3;
    double longitudeOriginRad = longitudeOrigin * (FastMath.PI / 180.0);

    char UTMZone = UTMRef.getUTMLatitudeZoneLetter(latitude);

    double ePrimeSquared = (eSquared) / (1 - eSquared);

    double n =
        a
            / FastMath.sqrt(1 - eSquared * FastMath.sin(latitudeRad)
                * FastMath.sin(latitudeRad));
    double t = FastMath.tan(latitudeRad) * FastMath.tan(latitudeRad);
    double c = ePrimeSquared * FastMath.cos(latitudeRad) * FastMath.cos(latitudeRad);
    double A = FastMath.cos(latitudeRad) * (longitudeRad - longitudeOriginRad);

    double M =
        a
            * ((1 - eSquared / 4 - 3 * eSquared * eSquared / 64 - 5 * eSquared
                * eSquared * eSquared / 256)
                * latitudeRad
                - (3 * eSquared / 8 + 3 * eSquared * eSquared / 32 + 45
                    * eSquared * eSquared * eSquared / 1024)
                * FastMath.sin(2 * latitudeRad)
                + (15 * eSquared * eSquared / 256 + 45 * eSquared * eSquared
                    * eSquared / 1024) * FastMath.sin(4 * latitudeRad) - (35
                * eSquared * eSquared * eSquared / 3072)
                * FastMath.sin(6 * latitudeRad));

    double UTMEasting =
        (UTM_F0
            * n
            * (A + (1 - t + c) * FastMath.pow(A, 3.0) / 6 + (5 - 18 * t + t * t
                + 72 * c - 58 * ePrimeSquared)
                * FastMath.pow(A, 5.0) / 120) + 500000.0);

    double UTMNorthing =
        (UTM_F0 * (M + n
            * FastMath.tan(latitudeRad)
            * (A * A / 2 + (5 - t + (9 * c) + (4 * c * c)) * FastMath.pow(A, 4.0)
                / 24 + (61 - (58 * t) + (t * t) + (600 * c) - (330 * ePrimeSquared))
                * FastMath.pow(A, 6.0) / 720)));

    // Adjust for the southern hemisphere
    if (latitude < 0) {
      UTMNorthing += 10000000.0;
    }

    return new UTMRef(UTMEasting, UTMNorthing, UTMZone, longitudeZone);
  }


  /**
   * Convert this LatLng from the OSGB36 datum to the WGS84 datum using an
   * approximate Helmert transformation.
   * 
   * @since 1.0
   */
  public void toWGS84() {
    double a = RefEll.AIRY_1830.getMaj();
    double eSquared = RefEll.AIRY_1830.getEcc();
    double phi = FastMath.toRadians(lat);
    double lambda = FastMath.toRadians(lng);
    double v = a / (FastMath.sqrt(1 - eSquared * Util.sinSquared(phi)));
    double H = 0; // height
    double x = (v + H) * FastMath.cos(phi) * FastMath.cos(lambda);
    double y = (v + H) * FastMath.cos(phi) * FastMath.sin(lambda);
    double z = ((1 - eSquared) * v + H) * FastMath.sin(phi);

    double tx = 446.448;
    double ty = -124.157;
    double tz = 542.060;
    double s = -0.0000204894;
    double rx = FastMath.toRadians(0.00004172222);
    double ry = FastMath.toRadians(0.00006861111);
    double rz = FastMath.toRadians(0.00023391666);

    double xB = tx + (x * (1 + s)) + (-rx * y) + (ry * z);
    double yB = ty + (rz * x) + (y * (1 + s)) + (-rx * z);
    double zB = tz + (-ry * x) + (rx * y) + (z * (1 + s));

    a = RefEll.WGS84.getMaj();
    eSquared = RefEll.WGS84.getEcc();

    double lambdaB = FastMath.toDegrees(FastMath.atan(yB / xB));
    double p = FastMath.sqrt((xB * xB) + (yB * yB));
    double phiN = FastMath.atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (FastMath.sqrt(1 - eSquared * Util.sinSquared(phiN)));
      double phiN1 = FastMath.atan((zB + (eSquared * v * FastMath.sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = FastMath.toDegrees(phiN);

    lat = phiB;
    lng = lambdaB;
  }


  /**
   * Convert this LatLng from the WGS84 datum to the OSGB36 datum using an
   * approximate Helmert transformation.
   * 
   * @since 1.0
   */
  public void toOSGB36() {
    RefEll wgs84 = new RefEll(6378137.000, 6356752.3141);
    double a = wgs84.getMaj();
    double eSquared = wgs84.getEcc();
    double phi = FastMath.toRadians(this.lat);
    double lambda = FastMath.toRadians(this.lng);
    double v = a / (FastMath.sqrt(1 - eSquared * Util.sinSquared(phi)));
    double H = 0; // height
    double x = (v + H) * FastMath.cos(phi) * FastMath.cos(lambda);
    double y = (v + H) * FastMath.cos(phi) * FastMath.sin(lambda);
    double z = ((1 - eSquared) * v + H) * FastMath.sin(phi);

    double tx = -446.448;
    double ty = 124.157;
    double tz = -542.060;
    double s = 0.0000204894;
    double rx = FastMath.toRadians(-0.00004172222);
    double ry = FastMath.toRadians(-0.00006861111);
    double rz = FastMath.toRadians(-0.00023391666);

    double xB = tx + (x * (1 + s)) + (-rx * y) + (ry * z);
    double yB = ty + (rz * x) + (y * (1 + s)) + (-rx * z);
    double zB = tz + (-ry * x) + (rx * y) + (z * (1 + s));

    RefEll airy1830 = new RefEll(6377563.396, 6356256.909);
    a = airy1830.getMaj();
    eSquared = airy1830.getEcc();

    double lambdaB = FastMath.toDegrees(FastMath.atan(yB / xB));
    double p = FastMath.sqrt((xB * xB) + (yB * yB));
    double phiN = FastMath.atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (FastMath.sqrt(1 - eSquared * Util.sinSquared(phiN)));
      double phiN1 = FastMath.atan((zB + (eSquared * v * FastMath.sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = FastMath.toDegrees(phiN);

    lat = phiB;
    lng = lambdaB;
  }


  /**
   * Calculate the surface distance in kilometres from the this LatLng to the
   * given LatLng.
   * 
   * @param ll
   * @return the surface distance in km
   * @since 1.0
   */
  public double distanceInKM(LatLon ll) {
    double er = 6366.707;

    double latFrom = FastMath.toRadians(getLat());
    double latTo = FastMath.toRadians(ll.getLat());
    double lngFrom = FastMath.toRadians(getLon());
    double lngTo = FastMath.toRadians(ll.getLon());
    
    double acos = FastMath.sin(latFrom) * FastMath.sin(latTo) + FastMath.cos(latFrom)
            * FastMath.cos(latTo) * FastMath.cos(lngTo - lngFrom);
    
    if(acos > 1)
    	acos = 1;
    if(acos < -1)
    	acos = -1;

    double d =
        FastMath.acos(acos)
            * er;

    return d;
  }
  
  /**
   * Calculate the surface distance in meters from the this LatLng to the
   * given LatLng.
   * 
   * @param ll
   * @return the surface distance in meters
   * @since 1.0
   */
  public double distanceInMeters(LatLon ll) {
	  return distanceInKM(ll)*1000;
  }


  /**
   * Return the latitude in degrees.
   * 
   * @return the latitude in degrees
   * @since 1.0
   */
  public double getLat() {
    return lat;
  }


  /**
   * Return the longitude in degrees.
   * 
   * @return the longitude in degrees
   * @since 1.0
   */
  public double getLon() {
    return lng;
  }

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LatLon) {
			return ((LatLon) obj).lat == lat && ((LatLon) obj).lng == lng;
		} else {
			return false;
		}
	}
}