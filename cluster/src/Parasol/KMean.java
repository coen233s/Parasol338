package Parasol;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

public class KMean {

	public static Color[][] extractColors(BufferedImage img) {
		int height = img.getHeight();
		int width = img.getWidth();
		Color[][] colors = new Color[width][height];
		for (int h=0; h<height; h++) {
			for (int w=0; w<width; w++) {
				colors[w][h] = new Color(img.getRGB(w,h));
			}
		}
		return colors;
	}
	
	public static void updateBufferedImage(BufferedImage src, Color[][] colors) {
		int height = src.getHeight();
		int width = src.getWidth();
		for (int h=0; h<height; h++) {
			for (int w=0; w<width; w++) {
				src.setRGB(w, h, colors[w][h].getRGB());
			}
		}
	}
	
	public static void updateColors(Integer[][] groups, Color[] centroids, Color[][] colors) {
		int width = colors.length;
		int height = colors[0].length;
		for (int h=0; h<height; h++) {
			for (int w=0; w<width; w++) {
				colors[w][h] = centroids[groups[w][h]];
			}
		}
	}
	
	public static int distanceSquare(Color src, Color dst) {
		int difRed = (src.getRed() - dst.getRed());
		int difGrn = (src.getGreen() - dst.getGreen());
		int difBlue = (src.getBlue() - dst.getBlue());
		return (difRed*difRed + difGrn*difGrn + difBlue*difBlue);
	}
	
	public static void updateGroupAssignment(Integer[][] groups, Color[] centroids, Color[][] colors) {
		int width = colors.length;
		int height = colors[0].length;
		for (int h=0; h<height; h++) {
			for (int w=0; w<width; w++) {
				// find the closest centroids
				int minGroup = -1;
				int minDistance = Integer.MAX_VALUE;
				Color c = colors[w][h];
				for (int k=0; k<centroids.length; k++) {
					int dist = distanceSquare(c,centroids[k]);
					if (dist < minDistance) {
						minGroup = k;
						minDistance = dist;
					}
				}
				assert(minGroup != -1);
				groups[w][h] = minGroup;
			}
		}
	}
	
	public static void updateCentroids(Integer[][] groups, Color[] centroids, Color[][] colors) {
		double[] redAvg = new double[centroids.length];
		double[] greenAvg = new double[centroids.length];
		double[] blueAvg = new double[centroids.length];
		Integer[] memberCount = new Integer[centroids.length];
		// init initial values
		for (int k=0; k<centroids.length; k++) {
			redAvg[k] = 0;
			greenAvg[k] = 0;
			blueAvg[k] = 0;
			memberCount[k] = 0;
		}
		// compute the new centroids
		int width = colors.length;
		int height = colors[0].length;
		for (int h=0; h<height; h++) {
			for (int w=0; w<width; w++) {
				int groupNum = groups[w][h];
				memberCount[groupNum]++;
				redAvg[groupNum] = redAvg[groupNum] + (colors[w][h].getRed()-redAvg[groupNum])/(memberCount[groupNum]+1);
				greenAvg[groupNum] = greenAvg[groupNum] + (colors[w][h].getGreen()-greenAvg[groupNum])/(memberCount[groupNum]+1);
				blueAvg[groupNum] = blueAvg[groupNum] + (colors[w][h].getBlue()-blueAvg[groupNum])/(memberCount[groupNum]+1);
			}
		}
		// reassign centroids
		for (int k=0; k<centroids.length; k++) {
			centroids[k] = new Color(	(int)redAvg[k],
										(int)greenAvg[k],
										(int)blueAvg[k]);
		}
	}
	
	public static Color[] CreateRandomCentroids(int n) {
		Color[] centroids = new Color[n];
		Random rnd = new Random();
		for (int k=0; k<n; k++) {
			centroids[k] = new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
		}
		return centroids;
	}
	
	public static final int NUM_CENTROIDS = 256;
	
	public static void main(String[] args) {
		BufferedImage img = null;
		try {
			// init steps
			img = ImageIO.read(new File("bird_large.jpg"));
		    Color[][] colors = extractColors(img);
		    Integer[][] groups = new Integer[colors.length][colors[0].length];
		    Color[] centroids = CreateRandomCentroids(NUM_CENTROIDS);
		    
		    // update steps
		    for (int i=0; i<100; i++) {
		    	updateGroupAssignment(groups, centroids, colors);
		    	updateCentroids(groups, centroids, colors);
		    	System.out.println("iteration: " + i);
		    }
		    
		    updateColors(groups, centroids, colors);
		    updateBufferedImage(img,colors);
		    String filename = "saved" + NUM_CENTROIDS + ".png";
		    File outputfile = new File(filename);
		    ImageIO.write(img, "png", outputfile);	
		} 
		catch (IOException e) {
			System.err.println("Failed to load this image.");
		}
	}
}
