/**
 * @author Danke Xie
 * @version 3/7/13
 */

package Parasol;

public class Triple<S,T,U> {

	public final S first;
	public final T second;
	public final U third;
	
	public Triple(S first, T second, U third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
