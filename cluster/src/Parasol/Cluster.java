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

class DataVec extends Vector<Double> {
	public DataVec() {}
	public DataVec(int length) {
		for (int i = 0; i < length; i++)
			add(0D);
	}
};

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
	
	public static final int NUM_CENTROIDS = 5;	

	public static void printHelp() {
		System.out.println("Usage: " + "java Cluster.class" + " [-t] [-h] <inputfile>");
		System.out.println();
		System.out.println("  -t\tTest");
		System.out.println("  -h\tHelp");
		System.out.println();
		System.out.println("Input file format:");
		System.out.println("<number of features N>");
		System.out.println("<filename0> <feature0> <feature1> <featureN-1>");
		System.out.println("<filename1> <feature0> <feature1> <featureN-1>");
		System.out.println("...");
	}

	static void LogMsg(String msg) {
		if (enableLog)
			System.out.println(msg);
	}	

	public Cluster(InputStream srcData) {
		m_sourceData = srcData;
		m_fileNames = new LinkedList<String>();
		m_feature = new LinkedList<DataVec>();
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
		Integer[] memberCount = new Integer[centroids.length];	
		
		for (int k = 0; k < centroids.length; k++) {
			avgData[k] = new DataVec(m_nDimensions);
			memberCount[k] = 0;
		}

		// compute the new centroids
		Iterator<DataVec> featureIt = features.iterator();

		// Loop over all files
		int data = 0;
		for (DataVec dv : features) {
			DataVec feature = featureIt.next();
			int groupNum = groups[data++];
			memberCount[groupNum]++;
			for (int i = 0; i < m_nDimensions; i++) {
				avgData[groupNum].set(i, avgData[groupNum].get(i) + (feature.get(i) - avgData[groupNum].get(i)) /
						(memberCount[groupNum] + 1));
			}			
		}
		// reassign centroids
		for (int k=0; k<centroids.length; k++) {
			centroids[k] = avgData[k];
		}
	}	

	public boolean cluster(PrintStream out, int iterLim, double dataMin, double dataMax) {
		Integer[] groups = new Integer[m_nSample];
		DataVec[] centroids = CreateRandomCentroids(m_nClusters, dataMin, dataMax);

		for (int i = 0; i < groups.length; i++)
			groups[i] = 0;
		
		// update steps
		for (int i = 0; i < iterLim; i++) {
			updateGroupAssignment(groups, centroids, m_feature);
			updateCentroids(groups, centroids, m_feature);
			System.out.println("iteration: " + i);
		}
		
		// print labels
		Iterator<String> itName = m_fileNames.iterator();
		for (int i = 0; i < m_nSample; i++) {
			String dataName = itName.next();
			out.println(dataName + "\t" + groups[i]);
		}
		
		return true;
	}
	
	public boolean process(PrintStream out, int nClusters) {
		m_nClusters = nClusters;

		boolean res = readData();
		if (!res)
			return false;

		res = cluster(out, ITERATIONS, m_dataMin, m_dataMax);
		if (!res)
			return false;
		
		return res;
	}

	@SuppressWarnings("deprecation")
	static void selfTest() {
		StringBuffer sb = new StringBuffer();

		final int nDim = 2;
		final int nSample = 10;
		final int nCluster = 2;

		Random r = new Random();

		// construct the test data
		sb.append(Integer.toString(nDim));
		sb.append("\n");
		for (int s = 1; s <= nSample; s++) {
			sb.append("Data_" + Integer.toString(s));
			for (int i = 0; i < nDim; i++) {
				double data;
				if (s <= nSample / 2) {
					data = r.nextGaussian() * 1 - 10;
				} else {
					data = r.nextGaussian() * 1 + 10;
				}

				sb.append("\t");
				sb.append(Double.toString(data));
			}
			sb.append("\n");
		}

		Cluster cluster = new Cluster(new StringBufferInputStream(sb.toString()));
		cluster.process(System.out, nCluster);
	}

	public static void main(String[] args) {
		boolean isTest = false;
		boolean showHelp = false;

		String dataPath = null;

		for (String arg : args) {
			if (arg.equalsIgnoreCase("-t"))
				isTest = true;
			else if (arg.equalsIgnoreCase("-?") || arg.equalsIgnoreCase("-h") ||
					arg.equalsIgnoreCase("--help"))
				showHelp = true;
			else {
				if (dataPath == null)
					dataPath = arg;
			}
		}

		if (showHelp) {
			printHelp();
			return;
		}

		if (isTest) {
			selfTest();
			return;
		}

		if (dataPath == null) {
			printHelp();
			return;
		}

		Cluster proc;
		try {
			proc = new Cluster(new FileInputStream(dataPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		proc.process(System.out, NUM_CENTROIDS);
	}
}
