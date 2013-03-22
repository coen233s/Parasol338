package Parasol;

import java.util.Vector;

class DataVec extends Vector<Double> {
	public DataVec() { }
	public DataVec(int num) {
		for (int i = 0; i < num; i++)
			add(0D);
	}
};