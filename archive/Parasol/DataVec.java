package Parasol;

import java.util.Vector;

class DataVec extends Vector<Double> {
	public DataVec() {}
	public DataVec(int length) {
		for (int i = 0; i < length; i++)
			add(0D);
	}
};