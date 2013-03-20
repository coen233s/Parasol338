package extract;

import java.io.IOException;
import java.io.OutputStreamWriter;
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
	
	public void printFeatures(OutputStreamWriter ps) {
		try {
			for (Double dval : featureVec) {
				ps.append(Double.toString(dval));
				ps.append("\t");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
}