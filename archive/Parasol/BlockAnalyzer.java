package Parasol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

class BlockList extends LinkedList<double[][]> { };

public class BlockAnalyzer {
	protected Vector<BlockList> m_blocks;
    protected List<DataVec> m_feature;
	protected BlockArchive m_blockArchive;
    
	public static final int BLOCK_SZ = 8;
	
	public static int[] jpegNaturalOrder = {
        0,  1,  8, 16,  9,  2,  3, 10,
       17, 24, 32, 25, 18, 11,  4,  5,
       12, 19, 26, 33, 40, 48, 41, 34,
       27, 20, 13,  6,  7, 14, 21, 28,
       35, 42, 49, 56, 57, 50, 43, 36,
       29, 22, 15, 23, 30, 37, 44, 51,
       58, 59, 52, 45, 38, 31, 39, 46,
       53, 60, 61, 54, 47, 55, 62, 63,
      };	
	
	public static int useDctFeature = 10;	
	
	public BlockAnalyzer(DCT dct) {
	    m_blockArchive = new BlockArchive(dct);
	    m_feature = new LinkedList<DataVec>();
	}
	
	// Called before encoding an image
	public void startImage() {
		m_blocks = new Vector<BlockList>();
		m_feature.clear();
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
	
	public Vector<double[][]> dataVecToBlock(int nComps, DataVec vec) {
	    Vector<double[][]> block = new Vector<double[][]>();
	    
	    for (int i = 0; i < nComps; i++) {
	        block.add(new double[BLOCK_SZ][BLOCK_SZ]);
	    }
	    
	    Iterator<Double> dataIt = vec.iterator();
	    
	    // Zigzag scan
		for (int z = 0; z < useDctFeature; z++) {
			int j = jpegNaturalOrder[z] / BLOCK_SZ;
			int k = jpegNaturalOrder[z] % BLOCK_SZ;
			block.get(0)[j][k] = dataIt.next();
			block.get(1)[j][k] = dataIt.next();
			block.get(2)[j][k] = dataIt.next();
		}

        return block;
	}	
	
	// Called after pushing all blocks from an image
	public void stopImage() {
		int nComps = m_blocks.size();
		assert(nComps == 3);
		
		// Pack the DCT features
		Iterator<double[][]> it1 = m_blocks.get(0).iterator();
		Iterator<double[][]> it2 = m_blocks.get(1).iterator();
		Iterator<double[][]> it3 = m_blocks.get(2).iterator();				

		for (int i = 0; i < m_blocks.get(0).size(); i++) {
		    DataVec vec = new DataVec();
		    
			double[][] Y = it1.next();
			double[][] Cb = it2.next();
			double[][] Cr = it3.next();
			
			// Only supports 4:4:4 JPEG format (maybe standard)
			assert(Cb.length == Y.length);
			assert(Cr.length == Y.length);
			
			// Zigzag scan
			for (int z = 0; z < useDctFeature; z++) {
				int j = jpegNaturalOrder[z] / 8;
				int k = jpegNaturalOrder[z] % 8;
				vec.add(Y[j][k]);
				vec.add(Cb[j][k]);
				vec.add(Cr[j][k]);
			}
			
			m_feature.add(vec);
		}

		// Number of blocks
		int nBlocks = m_feature.size();

		// Cluster parameters
		final int paramClusterNum = 40; // if members > this number, then add to dictionary
		final int paramCountThres = 10; // if members > this number, then add to dictionary

		// Cluster		
		Cluster cluster = new Cluster(m_feature, 0, 1); // check min and max
		Triple<Integer[], Integer[], DataVec[]> res = cluster.cluster(System.out, 
				paramClusterNum, 20);
		Integer[] groups = res.first;
		Integer[] memberCount = res.second;
		DataVec[] centroids = res.third;
		
		// Feature select
		for (int i = 0; i < memberCount.length; i++) {
		    if (memberCount[i] >= paramCountThres) {
		        Vector<double[][]> block = dataVecToBlock(nComps, centroids[i]);
		        m_blockArchive.add(block);
		    }
		}
	}
	
	public BlockArchive getBlockArchive() {
	    return m_blockArchive;
	}
}
