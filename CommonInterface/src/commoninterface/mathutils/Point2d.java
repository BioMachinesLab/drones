package commoninterface.mathutils;

import java.io.Serializable;

public class Point2d implements Serializable {
	public double x;
	public double y;

	public Point2d(double x, double y) {
		super();
		this.x = x;
		this.y = y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	public Point2d() {
		super();
		this.x = 0;
		this.y = 0;
	}

	public final double getX() {
		return x;
	}

	public final void setX(double x) {
		this.x = x;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	public final double getY() {
		return y;
	}

	public final void setY(double y) {
		this.y = y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");

	}

	public final void set(double x, double y) {
		this.x = x;
		this.y = y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");

	}

	public void set(Point2d newPos) {
		this.x = newPos.x;
		this.y = newPos.y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

}