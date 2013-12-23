package mo.umac.external.uscensus;

/**
 * Information of the divided grid
 * 
 * @author kate
 * 
 */
public class Grid {

	private int xOrder;

	private int yOrder;

	private double density;

	private Flag flag = Flag.UNVISITED;

	public enum Flag {
		VISITED, UNVISITED, BORDER, ZERO
	}

	public Grid() {

	}

	public Grid(Grid anotherGrid) {
		this.xOrder = anotherGrid.xOrder;
		this.yOrder = anotherGrid.yOrder;
		this.density = anotherGrid.density;
	}

	public int getxOrder() {
		return xOrder;
	}

	public void setxOrder(int xOrder) {
		this.xOrder = xOrder;
	}

	public int getyOrder() {
		return yOrder;
	}

	public void setyOrder(int yOrder) {
		this.yOrder = yOrder;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

}
