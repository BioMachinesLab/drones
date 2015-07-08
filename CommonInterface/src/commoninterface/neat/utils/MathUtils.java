/*
 * Created on 20-Oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package commoninterface.neat.utils;

import java.util.Random;

/**
 * @author msimmerson
 *
 * Provides some useful maths utility methods used for creating and modifying wieghts etc 
 */
public class MathUtils
{
	private static final Random rand = new Random();
    private MathUtils() {
    }
    
    // clamped to +plus/-minus
    public static double nextClampedDouble(double minus, double plus) {
    	return ((rand.nextDouble() - 0.5) * (plus - minus)); 	
    }
    
    public static double nextDouble() {
    	return (rand.nextDouble()); 	
    }

    public static double nextPlusMinusOne() {
    	return (nextClampedDouble(-1, 1));
    }
    
    public static double euclideanDist(double[] ePointOne, double[] ePoint2) {
    	double diff;
    	int i;
    	double eDist = 0;
    	
        for (i = 0; i < ePointOne.length; i++) {
        	if (i < ePoint2.length) {
        		diff = ePointOne[i] - ePoint2[i];
        	} else {
        		diff = 0;
        	}
        	eDist += (diff * diff);
        }
        eDist = Math.sqrt(eDist);
        
    	return (eDist);
    }
}
