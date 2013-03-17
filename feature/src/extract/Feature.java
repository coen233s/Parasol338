package extract;

public class Feature {
	protected ColorChannel<Double[][]> m_image;
	
	public Feature(	ColorChannel<Double[][]> image) {		
		this.m_image = image;
	}	
}
