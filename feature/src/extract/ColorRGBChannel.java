/**
 * @author un suthee
 * @version 3/7/13
 */

package extract;

public class ColorRGBChannel<T> {

	T r = null;
	T g = null;
	T b = null;

	public ColorRGBChannel() {
	}
	
	public ColorRGBChannel(T y, T cb, T cr) {
		this.r = y;
		this.g = cb;
		this.b = cr;
	}
	
	public T at(int index) {
		switch (index) {
		case 0: return r;
		case 1: return g;
		case 2: return b;
		}
		return null;
	}
	
	public void set(int index, T value) {
		switch (index) {
		case 0: r = value;
		case 1: g = value;
		case 2: b = value;
		}
	}	
}
