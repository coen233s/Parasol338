package subimg;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	public static void writeLargerTextFile(String aFileName, LinkedList<String> aLines) throws IOException {
		Path path = Paths.get(aFileName);
		try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)){
			for(String line : aLines){
				writer.write(line);
		        writer.newLine();
			}
		}
	}
}
