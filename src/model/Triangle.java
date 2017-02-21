package model;

import java.awt.Point;
import java.awt.geom.Point2D;

public class Triangle {
	
	public Point a, b, c;
	
	/** Constucteur */
	public Triangle(Point a, Point b, Point c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public Point[] getVertex() {
		return new Point[] {a, b, c};
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
		Point[] points = triangle.getVertex();
		for (int i = 0; i < 3; i++) {
			if (!(points[i].equals(a) || points[i].equals(b) || points[i].equals(c))) {
				return false;
			}
		}
		return true;
	}
	
	public boolean inCircum(Point p) {
		Point ab = new Point(b.x - a.x, b.y - a.y);
		double area = ((b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x)) / 2.0;
		Point a = this.a;
		Point b = this.b;
		Point c = this.c;
		if (area < 0) {
			Point tmp = b;
			b = c;
			c = tmp;
			area = -area;
		}
		double d_ab = a.distance(b);
		double d_bc = b.distance(c);
		double d_ca = c.distance(a);
		double R = (d_ab * d_bc * d_ca) / (4 * area);
		
		double dist = R * Math.sin( Math.acos((d_ab/2.0) / R) );
		Point2D n_ab = new Point.Double(-ab.y / d_ab, ab.x / d_ab);
		double radian_c = (b.x-c.x)*(a.x-c.x) + (b.y-c.y)*(a.y-c.y);
		if (radian_c < 0) {
			dist = -dist;
		}
		
		int cx = (int) (((a.x + b.x) / 2.0) + (dist * n_ab.getX()));
		int cy = (int) (((a.y + b.y) / 2.0) + (dist * n_ab.getY()));
		
		return (Point.distance(cx, cy, p.x, p.y) <= R);
	}
}
