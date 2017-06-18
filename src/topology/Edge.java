package topology;

import math2D.Point2D;

public class Edge {
	
	public Point2D a, b;
	
	/** Constructeur */
	public Edge(Point2D a, Point2D b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() * b.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		Edge edge = (Edge) o;
		return ((this.a == edge.a && this.b == edge.b) || (this.a == edge.b && this.b == edge.a));
	}
}
