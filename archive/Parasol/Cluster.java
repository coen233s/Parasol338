package Parasol;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Cluster {
	public static final String ENCODING = "UTF-8";
	public static final int ITERATIONS = 100;

	// Debugging
	static boolean enableLog = true;

	protected InputStream  m_sourceData;
	protected List<String> m_fileNames;
	protected List<DataVec> m_feature;
	protected int m_nDimensions;
	protected int m_nSample;
	protected int m_nClusters;

	protected double m_dataMin;
	protected double m_dataMax;
	
	protected Integer[] m_groups;
	protected Integer[] m_memberCount;
	
	public static final int NUM_CENTROIDS = 10;

	static void LogMsg(String msg) {
		if (enableLog)
			System.out.println(msg);
	}	

	public Cluster(List<DataVec> feature, double min, double max) {
		m_feature = feature;
		m_nSample = feature.size();
		m_dataMin = min;
		m_dataMax = max;
	}

	protected boolean readData() {
		int lineNum = 0;

		try {
			Reader reader = new InputStreamReader(m_sourceData,
					ENCODING);
			BufferedReader fin = new BufferedReader(reader);
			String line;
			line = fin.readLine();
			lineNum++;
			try {
				m_nDimensions = Integer.parseInt(line);
			}
			catch (java.lang.NumberFormatException e) {
				System.err.println("Line 1: should be the number of features");
				return false;
			}
			if (m_nDimensions == 0) {
				System.err.println("Line 1: number of features shouldn't be 0");
				return false;
			}

			m_dataMin = Double.MAX_VALUE;
			m_dataMax = Double.MIN_VALUE;
			
			for (;;) {
				line = fin.readLine();
				if (line == null)
					break;

				lineNum++;

				String[] tokens = line.split("\t");
				if (tokens.length != m_nDimensions + 1) {
					System.err.println("Line " + lineNum + ": wrong number of fields, " +
							"fields: " + tokens.length + " expected: " + (m_nDimensions + 1));
					return false;
				}
				m_fileNames.add(tokens[0]);	
				DataVec vec = new DataVec();
				for (int i = 1; i < tokens.length; i++) {
				    double data = Double.parseDouble(tokens[i]);
					vec.add(data);
				    m_dataMin = Math.min(m_dataMin, data);
				    m_dataMax = Math.max(m_dataMax, data);
				}
				m_feature.add(vec);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		m_nSample = m_fileNames.size();

		return true;
	}

	public DataVec[] CreateRandomCentroids(int n, double dataMin, double dataMax) {
		DataVec[] centroids = new DataVec[n];
		Random rnd = new Random();
		for (int k=0; k<n; k++) {
			centroids[k] = new DataVec();
			for (int l = 0; l < m_nDimensions; l++)
			    centroids[k].add(rnd.nextDouble() * (dataMax - dataMin) - dataMin); 
		}
		return centroids;
	}

	public double distanceSquare(DataVec src, DataVec dst) {
		double ss = 0;

		for (int i = 0; i < src.size(); i++) {
			double diff = src.get(i) - dst.get(i);
			ss += diff * diff;
		}

		return ss;
	}

	public void updateGroupAssignment(Integer[] groups, DataVec[] centroids, List<DataVec> features) {
		int data = 0;
		for (DataVec v : features) {
			// find the closest centroids
			int minGroup = -1;
			double minDistance = Double.MAX_VALUE;				
			for (int k=0; k < centroids.length; k++) {
				double dist = distanceSquare(v, centroids[k]);
				if (dist < minDistance) {
					minGroup = k;
					minDistance = dist;
				}
			}
			assert(minGroup != -1);
			groups[data] = minGroup;
			data++;
		}
	}

	public void updateCentroids(Integer[] groups, DataVec[] centroids, List<DataVec> features) {
		DataVec[] avgData = new DataVec[centroids.length];
		m_memberCount = new Integer[centroids.length];	
		
		for (int k = 0; k < centroids.length; k++) {
			avgData[k] = new DataVec(m_nDimensions);
			m_memberCount[k] = 0;
		}

		// compute the new centroids
		Iterator<DataVec> featureIt = features.iterator();

		// Loop over all files
		int data = 0;
		for (DataVec dv : features) {
			DataVec feature = featureIt.next();
			int groupNum = groups[data++];
			m_memberCount[groupNum]++;
			for (int i = 0; i < m_nDimensions; i++) {
				avgData[groupNum].set(i, avgData[groupNum].get(i) + (feature.get(i) - avgData[groupNum].get(i)) /
						(m_memberCount[groupNum] + 1));
			}			
		}
		// reassign centroids
		for (int k=0; k<centroids.length; k++) {
			centroids[k] = avgData[k];
		}
	}

	public Triple<Integer[], Integer[], DataVec[]> cluster(PrintStream out, int clusters,
	        int iterLim) {
	    m_groups = new Integer[m_nSample];
	    DataVec[] centroids = CreateRandomCentroids(m_nClusters, m_dataMin, m_dataMax);

	    m_nClusters = clusters;

	    for (int i = 0; i < m_groups.length; i++)
	        m_groups[i] = 0;

	    // update steps
	    for (int i = 0; i < iterLim; i++) {
	        updateGroupAssignment(m_groups, centroids, m_feature);
	        updateCentroids(m_groups, centroids, m_feature);
	        System.err.println("iteration: " + i);
	    }

	    // print labels
	    Iterator<String> itName = m_fileNames.iterator();
	    for (int i = 0; i < m_nSample; i++) {
	        String dataName = itName.next();
	        out.println(dataName + "\t" + m_groups[i]);
	    }

	    Triple<Integer[], Integer[], DataVec[]> res = new Triple<Integer[], Integer[], 
	    DataVec[]>(
	            m_groups,
	            m_memberCount,
	            centroids);

	    return res;
	}
}
