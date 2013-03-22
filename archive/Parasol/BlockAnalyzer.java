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
	
	public BlockAnalyzer() {
	    m_blockArchive = new BlockArchive();
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
	    int blockSize = (int)Math.sqrt(vec.size() / nComps);
	    
	    Vector<double[][]> block = new Vector<double[][]>();
	    
	    for (int i = 0; i < nComps; i++) {
	        block.add(new double[blockSize][blockSize]);
	    }
	    
	    Iterator<Double> dataIt = vec.iterator();
	    
	    for (int i = 0; i < blockSize; i++)
	        for (int j = 0; j < blockSize; j++) {
	                block.get(0)[i][j] = dataIt.next();
	                block.get(1)[i][j] = dataIt.next();
	                block.get(2)[i][j] = dataIt.next();
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
			
			for (int j = 0; j < Y.length; j++)
			    for (int k = 0; k < Y[0].length; k++) {
			        vec.add(Y[j][k]);
			        vec.add(Cb[j][k]);
			        vec.add(Cr[j][k]);
			}
			
			m_feature.add(vec);
		}

		// Number of blocks
		int nBlocks = m_feature.size();

		// Cluster
		Cluster cluster = new Cluster(m_feature, 0, 1); // check min and max
		Triple<Integer[], Integer[], DataVec[]> res = cluster.cluster(System.out, nBlocks / 10, 30);
		Integer[] groups = res.first;
		Integer[] memberCount = res.second;
		DataVec[] centroids = res.third;
		
		// Feature select
		final int paramCountThres = 10; // if members > this number, then add to dictionary
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
