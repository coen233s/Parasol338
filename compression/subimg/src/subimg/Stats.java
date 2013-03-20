package subimg;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

public class Stats {

	public static double calcMAD( 	final Double[][] refColors, int tx, int ty,
									final Double[][] srcColors, int sx, int sy, 
									int MBSize) {
		double sum = 0;
		for (int offY = 0; offY < MBSize; offY++) {
			for (int offX = 0; offX < MBSize; offX++) {
				double d = Math.abs(refColors[tx+offX][ty+offY] - srcColors[sx+offX][sy+offY]);
				sum += d;
			}
		}
		return (double)(sum)/((double)MBSize*(double)MBSize);
	}
	
	final static String ENCODING = "UTF-8";
	
	public static void writeLargerTextFile(String aFileName, LinkedList<String> aLines) throws IOException {
	    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(aFileName),
	            ENCODING);
	    for(String line : aLines){
	        writer.write(line);
	        writer.write("\n");
	    }
	}
}
