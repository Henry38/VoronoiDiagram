package topology;

import math2D.Point2D;

public class Triangle {
	
	private Point2D a, b, c;
	
	/** Constucteur */
	public Triangle(Point2D a, Point2D b, Point2D c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public Point2D[] getVertex() {
		return new Point2D[] {a, b, c};
	}
	
	public Edge[] getEdges() {
		return new Edge[] {new Edge(a, b), new Edge(b, c), new Edge(c, a)};
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() * b.hashCode() * c.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		Triangle triangle = (Triangle) o;
		Point2D[] points = triangle.getVertex();
		for (int i = 0; i < 3; i++) {
			if (!(points[i].equals(a) || points[i].equals(b) || points[i].equals(c))) {
				return false;
			}
		}
		return true;
	}
	
	public Point2D getCircumCenter() {
		Point2D half_ab = new Point2D((a.x+b.x)/2.0, (a.y+b.y)/2.0);
		Point2D half_bc = new Point2D((b.x+c.x)/2.0, (b.y+c.y)/2.0);
		//Point2D half_ca = new Point2D.Double((c.x+a.x)/2.0, (c.y+a.y)/2.0);
		
		Point2D dir_ab = new Point2D(b.x-a.x, b.y-a.y);
		Point2D dir_bc = new Point2D(c.x-b.x, c.y-b.y);
		//Point2D dir_ca = new Point2D.Double(-(a.y-c.y), a.x-c.x);
		
		double a1 = dir_ab.getX();
		double b1 = dir_ab.getY();
		double a2 = dir_bc.getX();
		double b2 = dir_bc.getY();
		
		double det = (a1 * b2) - (b1 * a2);
		if (det == 0) {
			return null;
		}
		
		double d1 = (a1 * half_ab.x) + (b1 * half_ab.y);
		double d2 = (a2 * half_bc.x) + (b2 * half_bc.y);
		
		double cx = (( b2 * d1) + (-b1 * d2)) / det;
		double cy = ((-a2 * d1) + ( a1 * d2)) / det;
		
		return new Point2D(cx, cy);
	}
	
	public boolean inCircum(Point2D p) {
		Point2D circumCenter = getCircumCenter();
		
		if (circumCenter == null) {
			return false;
		}
		
		double area = Math.abs( ((b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x)) / 2.0 );
		
		double d_ab = a.distance(b);
		double d_bc = b.distance(c);
		double d_ca = c.distance(a);
		double R = (d_ab * d_bc * d_ca) / (4 * area);
		
		return (Point2D.distance(circumCenter, p) <= R);
	}
}
