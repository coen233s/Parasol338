/**
 *  SubImg : 
 *  Generate motion vectors and residuals for the given image directory
 * 
 *  @author 	Un Suthee
 *  @version 	3/7/13
 *  
 */

package subimg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class SubImg {

	// Control parameters
	static int MBSize = 16; // a size of macro block
	static int MBHalfSize = MBSize / 2; // half of macro block
	
	// Data
	protected ColorChannel<Double[][]> refImage;
	
	// Debugging
	static boolean enableLog = true;

	public SubImg(File refImgName, File srcImgPath) {
		try {
			BufferedImage image = ImageIO.read(refImgName);
			refImage = ImgProcessing.extractColors(image);
		
			LogMsg("Load Reference Image. Done.");
			
			File[] files = srcImgPath.listFiles();
			for (File srcImgFile : files) {
				ColorChannel<Double[][]> srcImage = ImgProcessing.extractColors(ImageIO.read(srcImgFile));
				EncodeImage encodeImage = doMotionEst(refImage,srcImage);
				
				LogMsg(String.format("Compressed %s. Done.",srcImgFile.toString()));
			}
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image.");
		}
	}

	public static EncodeImage doMotionEst(	final ColorChannel<Double[][]> refImg, 
											final ColorChannel<Double[][]> srcImg) {
		if (srcImg == null)
			return null;
		
		ArrayList<MotionVect> mvs = new ArrayList<MotionVect>();
		ColorChannel<ArrayList<Double[][]>> residualChannel = new ColorChannel<ArrayList<Double[][]>>();
		
		// Calculate motion vectors for each MB
		Double[][] refColors = refImg.y;
		Double[][] srcColors = srcImg.y;
		int tw = refColors.length;
		int th = refColors[0].length;
		int sw = srcColors.length;
		int sh = srcColors[0].length;
		
		System.out.println(tw + "," + th + "," + sw + "," + sh);
		
		// For each MB from the source image
		for (int sx=0; sx<sw; sx+=MBSize) {
			System.out.println(sx);
			for (int sy=0; sy<sh; sy+=MBSize) {
				//System.out.println(sx + "," + sy);

				// For each MB from the reference image
				MotionVect mv = new MotionVect();
				double leastDiff = Double.MAX_VALUE;
				
			search:
				for (int ty=0; ty<th-MBSize; ty++) {
					for (int tx=0; tx<tw-MBSize; tx++) {
						double d = Stats.calcMAD(refColors,tx,ty,srcColors,sx,sy,MBSize);
						if (d < leastDiff) {
							leastDiff = d;
							mv.dx = tx - sx;
							mv.dy = ty - sy;
						}
						if (d <= 0.0)
							break search;
					}
				}
				//System.out.println("Least Diff=" + leastDiff + " mv=" + mv);
				mvs.add(mv);
			}
		}
		
		LogMsg("Done Creating Motion Vector");
		
		// create residue
		residualChannel.y = constructResidues(refImg.y, srcImg.y, mvs);
		LogMsg("Done Create residues for Y");
		
		residualChannel.cb = constructResidues(refImg.cb, srcImg.cb, mvs);
		residualChannel.cr = constructResidues(refImg.cr, srcImg.cr, mvs);
		
		EncodeImage.HeaderParams param = new EncodeImage.HeaderParams(sw,sh,MBSize);
		param.width = sw;
		param.height = sh;
		param.MBSize = MBSize;
		param.MBHalfSize = MBHalfSize;
		return new EncodeImage(mvs,residualChannel,param);
	}
	
	public static ArrayList<Double[][]> constructResidues(Double[][] refImg, Double[][] encodeImg, ArrayList<MotionVect> mvs) {
		int tw = refImg.length;
		int th = refImg[0].length;
		int ew = encodeImg.length;
		int eh = encodeImg[0].length;
		
		//System.out.println("MVLength = " + mvs.size());
		
		ArrayList<Double[][]> residues = new ArrayList<Double[][]>();
		// For each MB from the source image
		int index = 0;
		for (int ex=0; ex<ew; ex+=MBSize) {
			for (int ey=0; ey<eh; ey+=MBSize) {
				
				//System.out.println("ex,ey=" + ex +"," +ey);
				
				MotionVect mv = mvs.get(index++);
				int tx = ex + mv.dx;
				int ty = ey + mv.dy;
				
				assert(tx < tw);
				assert(ty < th);
				
				Double[][] residue = new Double[MBSize][MBSize];
				for (int xx=0; xx<MBSize; xx++) {
					for (int yy=0; yy<MBSize; yy++) {
						residue[xx][yy] = encodeImg[ex+xx][ey+yy] - refImg[tx+xx][ty+yy]; 
					}
				}
				residues.add(residue);
			}
		}
		return residues;
	}
	
	public static ColorChannel<Double[][]> constructImage(ColorChannel<Double[][]> refImage, EncodeImage encodeImg) {
		int rw = refImage.y.length;
		int rh = refImage.y[0].length;
		int ew = encodeImg.params.width;
		int eh = encodeImg.params.height;
		ArrayList<MotionVect> mvs = encodeImg.mvs;
		ColorChannel<Double[][]> decodeImage = new ColorChannel<Double[][]>();
		
		// Foreach color channel
		for (int c=0; c<3; c++) {
			int index = 0;
			ArrayList<Double[][]> residues = encodeImg.residues.at(c);
			Double[][] ref = refImage.at(c);
			Double[][] decodeColor = new Double[ew][eh];
			for (int ex=0; ex<ew; ex+=MBSize) {
				for (int ey=0; ey<eh; ey+=MBSize) {
					MotionVect mv = mvs.get(index);
					int tx = ex + mv.dx;
					int ty = ey + mv.dy;
					
					Double[][] difImg = residues.get(index++);
					for (int xx=0; xx<MBSize; xx++) {
						for (int yy=0; yy<MBSize; yy++) {
							assert(tx+xx < rw);
							assert(ty+yy < rh);
							assert(tx+xx >= 0);
							assert(ty+yy >= 0);
							decodeColor[ex+xx][ey+yy] = difImg[xx][yy] + ref[tx+xx][ty+yy]; 
						}
					}
				}
			}
			decodeImage.set(c, decodeColor);
			for (int ex=0; ex<ew; ex++) {
				for (int ey=0; ey<eh; ey++) {
					assert(decodeImage.at(c)[ex][ey] != null);
				}
			}
		}
		return decodeImage;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static void LogMsg(String msg) {
		if (enableLog)
			System.out.println(msg);
	}
	
	static void selfTest() {
		test_doMotionEst();
	}
	
	static void test_doMotionEst() {
		System.out.println("Test SubImg.doMotionEst()...");
		
		try {
			// We will run motion estimation on the same image
			String filename = "box1.png";
			ColorChannel<Double[][]> refImage = ImgProcessing.extractColors(ImageIO.read(new File(Conf.TEST_DATA_PATH + filename)));
			LogMsg("Load Reference Image:" + filename + " ...");

			String encodeFilename = "box2.png";
			ColorChannel<Double[][]> srcImage = ImgProcessing.extractColors(ImageIO.read(new File(Conf.TEST_DATA_PATH + encodeFilename)));
			LogMsg("Load Source Image:" + filename + " ...");
			
			EncodeImage encodeImage = doMotionEst(refImage,srcImage);
			LogMsg("Done Motion Estimation.");
			
			encodeImage.writeMotionVectorImage("box");
			encodeImage.writeResiduesImage("box");
			
			LogMsg("Decoding image...");
			ColorChannel<Double[][]> decodeImg = constructImage(refImage, encodeImage);
			LogMsg("Done Decoding.");
			
			int width = decodeImg.y.length;
			int height = decodeImg.y[0].length;
			Color[][] rgb = ImgProcessing.fromYCbCy2RGB(decodeImg);
			BufferedImage outImg = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
			ImgProcessing.updateBufferedImage(outImg,rgb);
		    ImageIO.write(outImg, "png", new File(Conf.TEST_DATA_PATH + "new_block.png"));	
		 
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image.");
		}
		
		System.out.print("Done.");
	}
	
	public static void main(String[] args) {
		selfTest();
	}
}
