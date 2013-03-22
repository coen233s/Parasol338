package Parasol;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;

/*
 * This is a non-optimized block archive.
 * 
 * To optimize:
 * 1) The data should be indexable. (done)
 * 2) The data should be sorted, and supports binary search
 * 
 */
public class BlockArchive {
	DCT m_dct;
    Vector<Vector<double[][]>> m_blockLibrary;
    
    public BlockArchive(DCT dct) {
        m_blockLibrary = new Vector<Vector<double[][]>>();
        m_dct = dct;
    }
    
    public void add(Vector<double[][]> block) {
        m_blockLibrary.add(block);
    }
    
    protected double getDist(Vector<double[][]> a, Vector<double[][]> b) {
        double sumSq = 0.0;
        
        Iterator<double[][]> ai = a.iterator();
        Iterator<double[][]> bi = b.iterator();
        
        for (; ai.hasNext(); ) {
            double[][] ma = ai.next();
            double[][] mb = bi.next();
            
            for (int i = 0; i < ma.length; i++)
                for (int j = 0; j < ma[0].length; j++)
                    sumSq += (ma[i][j] - mb[i][j]) * (ma[i][j] - mb[i][j]);
        }
        
        return sumSq;
    }    
    
    public Color[][] fromYCbCy2RGB(Vector<double[][]> srcImg) {
        // YCbCr -> RGB matrix
        final double b12 = Double.parseDouble("-9.2674E-4");  
        final double b13 = Double.parseDouble("1.4017");  
        final double b22 = Double.parseDouble("-3.4370E-1");  
        final double b23 = Double.parseDouble("-7.1417E-1");  
        final double b32 = Double.parseDouble("1.7722");  
        final double b33 = Double.parseDouble("9.9022E-4");          
        
        int width = srcImg.get(0).length;
        int height = srcImg.get(0)[0].length;
        Color[][] dstImg = new Color[width][height];

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int r = (int) Math.round(srcImg.get(0)[x][y] + 
                        srcImg.get(1)[x][y] * b12 + srcImg.get(1)[x][y] * b13);
                int g = (int) Math.round(srcImg.get(0)[x][y] +
                        srcImg.get(1)[x][y] * b22 + srcImg.get(2)[x][y] * b23);
                int b = (int) Math.round(srcImg.get(0)[x][y] +
                        srcImg.get(1)[x][y] * b32 + srcImg.get(2)[x][y] * b33);
                dstImg[x][y] = new Color(r,g,b);
            }
        }
        return dstImg;
    }
    
    public void updateBufferedImage(BufferedImage src, Color[][] colors) {

        assert(src.getWidth() == colors.length);
        assert(src.getHeight() == colors[0].length);

        int width = src.getWidth();
        int height = src.getHeight();

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                src.setRGB(x, y, colors[x][y].getRGB());
            }
        }
    }    

    public void outputImage(int idx, String imageName) {
        BufferedImage outImg = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        Vector<double[][]> v = m_blockLibrary.get(idx);
        
        double[][] y = v.get(0);
        double[][] cb = v.get(1);
        double[][] cr = v.get(2);       
        
        double[][] yd = m_dct.inverseDCT(y);
        double[][] cbd = m_dct.inverseDCT(cb);
        double[][] crd = m_dct.inverseDCT(cr);
        
        Vector<double[][]> yuv = new Vector<double[][]>();
        yuv.add(yd);
        yuv.add(cbd);
        yuv.add(crd);
        
        updateBufferedImage(outImg, fromYCbCy2RGB(yuv));
        
        try {
            ImageIO.write(outImg, "png",
                    new File(imageName + "_" + idx + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Pair<Integer, Vector<double[][]>> findNearestBlock(Vector<double[][]> block) {
        double minDist = Double.MAX_VALUE;
        int bestBlock = -1;
        
        for (int i = 0; i < m_blockLibrary.size(); i++) {
            double dist = getDist(m_blockLibrary.get(i), block);
            if (dist <= minDist) {
                minDist = dist;
                bestBlock = i;
            }
        }
        
        Pair<Integer, Vector<double[][]>> res;
        if (bestBlock != -1)
            res = new Pair<Integer, Vector<double[][]>>(
                    bestBlock,
                    m_blockLibrary.get(bestBlock));
        else
            res = null;
        
        return res;
    }
    
    public void writeLibraryToFolder(String path) {
        for (int i = 0; i < m_blockLibrary.size(); i++) {
            outputImage(i, path + "/blk");
        }
    }
}
