package subimg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class EncodeImage {
	
	// For convenient-ness but we do not encode these fields
	public static class HeaderParams {
		
		public int width;
		public int height;
		public int MBSize;
		public int MBHalfSize;
		
		public HeaderParams(int w,int h,int mb) {
			this.width = w;
			this.height = h;
			this.MBSize = mb;
			this.MBHalfSize = mb / 2;
		}
	}
		
	ArrayList<MotionVect> mvs;
	ColorChannel<ArrayList<Double[][]>> residues;
	HeaderParams params;
	
	public EncodeImage(	ArrayList<MotionVect> mvs, 
						ColorChannel<ArrayList<Double[][]>> residues,
						HeaderParams p) {
		this.mvs = mvs;
		this.residues = residues;
		params = p;
	}
	
	public void writeResiduesImage(String imgName) {
		try {
			BufferedImage outImg = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
			
			int ew = params.width;
			int eh = params.height;
			
			// Foreach color channel
			for (int c=0; c<3; c++) {
				int index = 0;
				ArrayList<Double[][]> res = residues.at(c);
				Color[][] residueColor = new Color[ew][eh];
				for (int ex=0; ex<ew; ex+=params.MBSize) {
					for (int ey=0; ey<eh; ey+=params.MBSize) {
						Double[][] difImg = res.get(index++);
						for (int xx=0; xx<params.MBSize; xx++) {
							for (int yy=0; yy<params.MBSize; yy++) {
								int scaleColor = (int) Math.round(Math.max(0.0, Math.min((difImg[xx][yy]/2.0)+127.0,255.0)));
								//System.out.println("scaleColor = " + difImg[xx][yy] + ":" + scaleColor);
								residueColor[ex+xx][ey+yy] = new Color(scaleColor,scaleColor,scaleColor);
							}
						}
					}
				}
				ImgProcessing.updateBufferedImage(outImg,residueColor);
			    ImageIO.write(outImg, "png", new File(Conf.TEST_DATA_PATH + imgName + "_residue_" + c + ".png"));	
			}
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image.");
		}
	}
	
	public void writeMotionVectorImage(String imgName) {
		try {
			BufferedImage outImg = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
			
			int ew = params.width;
			int eh = params.height;
			int index = 0;
			Color[][] mvColor = new Color[ew][eh];
			for (int x=0; x<ew; x++) {
				for (int y=0; y<eh; y++) {
					mvColor[x][y] = new Color(0,0,255);
				}
			}
			
			for (int ex=0; ex<ew; ex+=params.MBSize) {
				for (int ey=0; ey<eh; ey+=params.MBSize) {
					MotionVect mv = mvs.get(index++);
					double scaled_x = (double)(mv.dx + ew/2)/(double)ew;
					double scaled_y = (double)(mv.dy + eh/2)/(double)eh;
					int xcolor = (int) Math.max(0, Math.min(Math.round(scaled_x * 255.0),255));
					int ycolor = (int) Math.max(0, Math.min(Math.round(scaled_y * 255.0),255));
					for (int xx=0; xx<4; xx++) {
						for (int yy=0; yy<4; yy++) {
							mvColor[ex+params.MBHalfSize-xx][ey+params.MBHalfSize-yy] = new Color(xcolor,xcolor,xcolor);
							mvColor[ex+params.MBHalfSize+1+xx][ey+params.MBHalfSize-yy] = new Color(ycolor,ycolor,ycolor);
						}
					}
				}
			}
			ImgProcessing.updateBufferedImage(outImg,mvColor);
			ImageIO.write(outImg, "png", new File(Conf.TEST_DATA_PATH + imgName + "_motionvect.png"));	
		}
		catch(IOException ex) {
			System.err.println("Failed to load the reference image.");
		}
	}
}
