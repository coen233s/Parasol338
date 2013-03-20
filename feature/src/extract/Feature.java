package extract;

import java.io.PrintStream;
import java.util.Vector;

/*
 * Feature extraction
 * 
 * Input:
 *    N x M mage
 *    
 * Output:
 *    a vector of feature values
 *    
 * Features:
 *    vector of Floats/Doubles
 */

public class Feature {
	protected ColorRGBChannel<Double[][]> m_image;
	protected Vector<Double> featureVec;
	
	public Feature(ColorRGBChannel<Double[][]> image) {		
		this.m_image = image;
		featureVec = new Vector<Double>();
	}
	
	public void printFeatures(PrintStream ps) {
		for (Double dval : featureVec) {
			ps.print(dval);
			ps.print("\t");
		}
		ps.println();
	}
}
