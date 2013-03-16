/**
 * @author un suthee
 * @version 3/7/13
 */

package subimg;

public class ColorChannel<T> {

	T y = null;
	T cb = null;
	T cr = null;

	public ColorChannel() {
	}
	
	public ColorChannel(T y, T cb, T cr) {
		this.y = y;
		this.cb = cb;
		this.cr = cr;
	}
	
	public T at(int index) {
		switch (index) {
		case 0: return y;
		case 1: return cb;
		case 2: return cr;
		}
		return null;
	}
	
	public void set(int index, T value) {
		switch (index) {
		case 0: y = value;
		case 1: cb = value;
		case 2: cr = value;
		}
	}
}
