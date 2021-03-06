package extract;

/*
 * Split the image into CX and CY blocks
 */

public class FeatureMeanColor extends Feature {
	public static final int m_cx = 40; 
	public static final int m_cy = 40;
	
	static public int getFeatureNumber() {
		return m_cx * m_cy * 3;
	}
	
	public FeatureMeanColor(ColorRGBChannel<Double[][]> image) {
		super(image);
		process();
	}
	
	public void process() {
		int imgCx = m_image.at(0).length;
		int imgCy = m_image.at(0)[0].length;
		
		int blockCx = imgCx / m_cx;
		int blockCy = imgCy / m_cy;
		
		for (int i = 0; i < m_cx; i++) {
		    for (int j = 0; j < m_cy; j++) {
				int startBlockCx = blockCx * i;
				int startBlockCy = blockCy * j;
				
				double rsum = 0, gsum = 0, bsum = 0;
				int nSample = 0;
				
				for (int k = 0; k < blockCx; k++)
					for (int l = 0; l < blockCy; l++) {
						rsum += m_image.at(0)[startBlockCx + k][startBlockCy + l];
						gsum += m_image.at(1)[startBlockCx + k][startBlockCy + l];
						bsum += m_image.at(2)[startBlockCx + k][startBlockCy + l];
						nSample++;
					}
				
				rsum /= nSample;
				gsum /= nSample;
				bsum /= nSample;
				
				featureVec.add(rsum);
				featureVec.add(gsum);
				featureVec.add(bsum);
			}
		}
	}
}
