package extract;

import java.io.BufferedWriter;
import java.io.IOException;
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
	
	public static void writeLargerTextFile(String aFileName, LinkedList<String> aLines) throws IOException {
		/*Path path = Paths.get(aFileName);
		try (BufferedWriter writer = Files.newBufferedWriter(path)){
			for(String line : aLines){
				writer.write(line);
		        writer.newLine();
			}
		}*/
	}
}
