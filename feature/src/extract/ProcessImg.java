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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

public class ProcessImg {
	public static final String ENCODING = "UTF-8";
	// Data
	protected ColorRGBChannel<Double[][]> refImage;
	
	// Debugging
	static boolean enableLog = true;

	protected File m_srcImgPath;
	
	public ProcessImg(File srcImgPath) {
		m_srcImgPath = srcImgPath;
	}
	
	public boolean process(OutputStreamWriter out) {
		try {
			File[] files = m_srcImgPath.listFiles();
			if (files == null) {
				System.err.println("No files found in directory " + m_srcImgPath);
				return false;
			}
			
			out.append(Integer.toString(FeatureMeanColor.getFeatureNumber()));
			out.append("\n");
			
			for (File srcImgFile : files) {
				out.append(srcImgFile.toString());
				out.append("\t");
				
				ColorRGBChannel<Double[][]> srcImage = ImgProcessing.extractColors(ImageIO.read(srcImgFile));
				Feature encodeImage = doFeatureExtract(srcImage);
				
				encodeImage.printFeatures(out);
				out.append("\n");
			}
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image. " +
					ex.getMessage());
			return false;
		}
		
		return true;
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
						 
			encodeImage.printFeatures(new OutputStreamWriter(System.out));
			System.out.println();
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image. " +
					ex.getMessage());
		}
		
		System.out.print("Done.");
	}
	
	public static void printHelp() {
		System.out.println("Usage: " + "java ProcessImg.class" + " [-t] [-h] [-o<output>] <path>");
		System.out.println();
		System.out.println("  -t\tTest");
		System.out.println("  -h\tHelp");
	}
	
	public static void main(String[] args) {
		boolean isTest = false;
		boolean showHelp = false;
		
		String imgPath = null;
		String outPath = null;
		
		for (String arg : args) {
			if (arg.equalsIgnoreCase("-t"))
				isTest = true;
			else if (arg.equalsIgnoreCase("-?") || arg.equalsIgnoreCase("-h") ||
					arg.equalsIgnoreCase("--help"))
				showHelp = true;
			else if (arg.substring(0, 2).equalsIgnoreCase("-o")) {
				outPath = arg.substring(2);
			} else {
				if (imgPath == null)
					imgPath = arg;
			}
		}
		
		if (showHelp) {
			printHelp();
			return;
		}
		
		if (isTest) {
			selfTest();
			return;
		}
		
		if (imgPath == null) {
			printHelp();
			return;
		}
						
		ProcessImg proc = new ProcessImg(new File(imgPath));
		OutputStreamWriter writer;
		
		if (outPath != null) {
			try {
				writer = new OutputStreamWriter(new FileOutputStream(outPath),
						ENCODING);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}
		} else {
			writer = new OutputStreamWriter(System.out);
		}
		
		proc.process(writer);
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
