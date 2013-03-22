package Parasol;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class SingleEncode {

	public static void printHelp() {
		System.out.println("Usage: " + "java SingleEncode.class" + " [-t] [-h] [-q<quality>] -o<outputfile> <inputfile>");
		System.out.println();
		System.out.println("  -o<outputfile>\tOutput filename");
		System.out.println("  -q<quality>\t\tQuality (0-100, 100=best)");
		System.out.println("  -t\t\t\tTest");
		System.out.println("  -h\t\t\tHelp");
		System.out.println();
		System.out.println("Input file format: JPEG, BMP, GIF, PNG, etc.");		
	}
	
	public static void jpegEncode(boolean archive, String srcFile, int quality, String outFile,
	        String blockPath) {
		File file = new File(srcFile);
		BufferedImage img;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		OutputStream out;
		try {
			out = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		if (archive) {
		    BlockAnalyzer analyze = new BlockAnalyzer();
		    JpegArchiveEncoder enc = new JpegArchiveEncoder(img, quality, out, analyze);
		    enc.Compress();
		    analyze.m_blockArchive.writeLibraryToFolder(blockPath);
		} else {		    
            JpegEncoder enc = new JpegEncoder(img, quality, out);
            enc.Compress();            		   
		}
	}
	
	static void testJPEGEncode() {
		int quality = 20;
		jpegEncode(false, "test_data/28.jpg", quality, "test_data/28_enc.jpg", null);
	}
	
    static void testJPEGArchive() {
        int quality = 20;
        jpegEncode(true, "test_data/28.jpg", quality, "test_data/28_enc.jpg",
                "test_data");
    }	
	
	public static void main(String[] args) {
		boolean isTest = false;
		boolean showHelp = false;

		String dataPath = null;
		String outPath = null;
		
		int quality = 20;
		int testId = -1;
		
		for (String arg : args) {
		    if (arg.substring(0, 2).equalsIgnoreCase("-t")) {
		        testId = Integer.parseInt(arg.substring(2));				
		    } else if (arg.equalsIgnoreCase("-?") || arg.equalsIgnoreCase("-h") ||
					arg.equalsIgnoreCase("--help"))
				showHelp = true;
			else if (arg.substring(0, 2).equalsIgnoreCase("-o")) {
				outPath = arg.substring(2);
			} else if (arg.substring(0, 2).equalsIgnoreCase("-q")) {
				quality = Integer.parseInt(arg.substring(2));
			} else {
				if (dataPath == null)
					dataPath = arg;
			}
		}

		if (showHelp) {
			printHelp();
			return;
		}

		switch (testId) {
		case 1: 		    
			testJPEGEncode();
			return;
		case 2:
		    testJPEGArchive();
			return;
		}

		if (dataPath == null || outPath == null) {
			printHelp();
			return;
		}
		
		System.err.println("Compressing image " + dataPath + " to " +
				outPath + " with quality " + quality);
		jpegEncode(false, dataPath, quality, outPath, null);
	}
}
