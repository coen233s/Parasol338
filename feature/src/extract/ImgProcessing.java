/**
 * @author un suthee
 * @version 3/7/13
 */

package extract;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImgProcessing {

	public static Color[][] extractRGBColors(BufferedImage img) {
		Color[][] colors = new Color[img.getWidth()][img.getHeight()];
		for (int y=0; y<img.getHeight(); y++) {
			for (int x=0; x<img.getWidth(); x++) {
				colors[x][y] = new Color(img.getRGB(x, y));
			}
		}
		return colors;
	}
	
	// RGB -> YCbCy matrix
	static final double a11 = 0.299;
	static final double a12 = 0.587;
	static final double a13 = 0.114;
	static final double a21 = -0.169;
	static final double a22 = -0.331;
	static final double a23 = 0.5;
	static final double a31 = 0.5;
	static final double a32 = -0.419;
	static final double a33 = -0.081;
	
	public static ColorRGBChannel<Double[][]> extractColors(BufferedImage img) {
		Double[][] Y = new Double[img.getWidth()][img.getHeight()];
		Double[][] Cb = new Double[img.getWidth()][img.getHeight()];
		Double[][] Cr = new Double[img.getWidth()][img.getHeight()];
		
		for (int x=0; x<img.getWidth(); x++) {
			for (int y=0; y<img.getHeight(); y++) {
				Color c = new Color(img.getRGB(x, y));
				
//				Y[x][y] = (double) c.getRed() * a11 + (double) c.getGreen() * a12 + (double) c.getBlue() * a13;
//				Cb[x][y] = (double) c.getRed() * a21 + (double) c.getGreen() * a22 + (double) c.getBlue() * a23;
//				Cr[x][y] = (double) c.getRed() * a31 + (double) c.getGreen() * a32 + (double) c.getBlue() * a33;

				Y[x][y] = (double) c.getRed();
				Cb[x][y] = (double) c.getGreen();
				Cr[x][y] = (double) c.getBlue();				
				
				assert(Y[x][y] >= 0.0 && Y[x][y] <= 255.0);
				assert(Cb[x][y] >= 0.0 && Cb[x][y] <= 255.0);
				assert(Cr[x][y] >= 0.0 && Cr[x][y] <= 255.0);
			}
		}
		return new ColorRGBChannel<Double[][]>(Y,Cb,Cr);
	}
	
	// YCbCr -> RGB matrix
	//static final double b11 = 1.0;  
	//static final double b21 = 1.0;
	//static final double b31 = 1.0;  
	static final double b12 = Double.parseDouble("-9.2674E-4");  
	static final double b13 = Double.parseDouble("1.4017");  
	static final double b22 = Double.parseDouble("-3.4370E-1");  
	static final double b23 = Double.parseDouble("-7.1417E-1");  
	static final double b32 = Double.parseDouble("1.7722");  
	static final double b33 = Double.parseDouble("9.9022E-4");  
	
	public static Color[][] fromYCbCy2RGB(ColorRGBChannel<Double[][]> srcImg) {
		int width = srcImg.r.length;
		int height = srcImg.r[0].length;
		Color[][] dstImg = new Color[width][height];
		
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				int r = (int) Math.round(srcImg.r[x][y] + srcImg.g[x][y] * b12 + srcImg.b[x][y] * b13);
				int g = (int) Math.round(srcImg.r[x][y] + srcImg.g[x][y] * b22 + srcImg.b[x][y] * b23);
				int b = (int) Math.round(srcImg.r[x][y] + srcImg.g[x][y] * b32 + srcImg.b[x][y] * b33);
				dstImg[x][y] = new Color(r,g,b);
			}
		}
		return dstImg;
	}
	
	public static void updateBufferedImage(BufferedImage src, Color[][] colors) {
		
		assert(src.getWidth() == colors.length);
		assert(src.getHeight() == colors[0].length);
		
		int width = src.getWidth();
		int height = src.getHeight();
		
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				src.setRGB(x, y, colors[x][y].getRGB());
			}
		}
	}	
}
