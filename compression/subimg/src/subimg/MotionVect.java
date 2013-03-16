/**
 * @author un suthee
 * @version 3/7/13
 */

package subimg;

public class MotionVect {

	int dx;
	int dy;
	
	public MotionVect() {
		this.dx = 0;
		this.dy = 0;
	}

	public MotionVect(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	@Override 
	public String toString() {
		return String.format("%d,%d",dx,dy);
	}
}
