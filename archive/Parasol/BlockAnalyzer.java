package Parasol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

class BlockList extends LinkedList<double[][]> { };

public class BlockAnalyzer {
	protected Vector<BlockList> m_blocks;
	
	public static final int BLOCK_SZ = 8;
	
	// Called before encoding an image
	public void start() {
		m_blocks = new Vector<BlockList>();
		for (int i = 0; i < 3; i++) {
			BlockList blockList = new BlockList();
			m_blocks.add(blockList);
		}
	}
	
	// Block analysis, called by encoder after the 8x8 block is DCT-transformed
	public void add(int comp, double[][] dctArray) {
		assert(dctArray.length == BLOCK_SZ && dctArray[0].length == BLOCK_SZ);		
		m_blocks.get(comp).add(dctArray);
	}
	
	// Called after pushing all blocks from an image
	public void analyze() {
		int nComps = m_blocks.size();
		
		Iterator<double[][]> it1 = m_blocks.get(0).iterator();
		Iterator<double[][]> it2 = m_blocks.get(1).iterator();
		Iterator<double[][]> it3 = m_blocks.get(2).iterator();
		
		for (int i = 0; i < m_blocks.get(0).size(); i++) {
			double[][] Y = it1.next();
			double[][] Cb = it2.next();
			double[][] Cr = it3.next();			
			
			DataVec vec = new DataVec();
			// here
			for (int j = 0; j < nComps; j++) {
				
			}
		}
	}
}
