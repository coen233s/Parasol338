/**
 *  SubImg : 
 *  Generate motion vectors and residuals for the given image directory
 * 
 *  @author 	Un Suthee
 *  @version 	3/7/13
 *  
 */

package extract;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import quicktime.util.EncodedImage;

public class ProcessImg {

	// Control parameters
	static int MBSize = 16; // a size of macro block
	static int MBHalfSize = MBSize / 2; // half of macro block
	
	// Data
	protected ColorRGBChannel<Double[][]> refImage;
	
	// Debugging
	static boolean enableLog = true;

	public ProcessImg(File srcImgPath) {
		try {
			File[] files = srcImgPath.listFiles();
			for (File srcImgFile : files) {
				LogMsg(String.format("Processing %s....",srcImgFile.toString()));
				
				ColorRGBChannel<Double[][]> srcImage = ImgProcessing.extractColors(ImageIO.read(srcImgFile));
				Feature encodeImage = doFeatureExtract(srcImage);
				
				LogMsg(String.format("Feature extraction %s. Done.",srcImgFile.toString()));
				
				encodeImage.printFeatures(System.out);
			}
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image. " +
					ex.getMessage());
		}
	}
	
	static Feature doFeatureExtract(ColorRGBChannel<Double[][]> srcImage)
	{
		return new FeatureMeanColor(srcImage);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static void LogMsg(String msg) {
		if (enableLog)
			System.out.println(msg);
	}
	
	static void selfTest() {
		test_doFeatureExtract();
	}
	
	static void test_doFeatureExtract() {
		System.out.println("Test SubImg.test_doFeatureExtract()...");
		
		try {
			// We will run motion estimation on the same image
			String filename = "box1.png";
			LogMsg("Load Reference Image: " + Conf.TEST_DATA_PATH + filename + " ...");			
			ColorRGBChannel<Double[][]> refImage = ImgProcessing.extractColors(
					ImageIO.read(new File(Conf.TEST_DATA_PATH + filename)));

			Feature encodeImage = doFeatureExtract(refImage);
			LogMsg("Done Feature Extraction.");
			
			encodeImage.printFeatures(System.out);
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image. " +
					ex.getMessage());
		}
		
		System.out.print("Done.");
	}
	
	public static void main(String[] args) {
		selfTest();
	}
}
