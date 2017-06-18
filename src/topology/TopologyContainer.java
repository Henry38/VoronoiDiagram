package topology;

import java.util.ArrayList;

import math2D.Point2D;

public class TopologyContainer {
	
	private ArrayList<Point2D> vertex;
	private ArrayList<int[]> polygons;
	
	/** Constructeur */
	public TopologyContainer() {
		this.vertex = new ArrayList<Point2D>();
		this.polygons = new ArrayList<int[]>();
	}
	
	public int getVertexCount() {
		return vertex.size();
	}
	
	public int getPolygonsCount() {
		return polygons.size();
	}
	
	public final ArrayList<Point2D> getVertex() {
		return vertex;
	}
	
	public Point2D[] getPolygon(int index) {
		int[] indices = polygons.get(index);
		int npoints = indices.length;
		Point2D[] polygon = new Point2D[npoints];
		for (int i = 0; i < npoints; i++) {
			polygon[i] = vertex.get(indices[i]);
		}
		return polygon;
		//return polygons.get(i);
	}
	
	public void clear() {
		this.vertex.clear();
		this.polygons.clear();
	}
	
	public void addPolygon(Point2D... points) {
		int[] polygon = new int[points.length];
		int i = 0;
		for (Point2D p : points) {
			int index = vertex.indexOf(p);
			if (index == -1) {
				vertex.add(p);
				index = vertex.size() - 1;
			}
			polygon[i] = index;
			i++;
		}
		polygons.add(polygon);
	}
	
	public void addTriangle(Point2D a, Point2D b, Point2D c) {
		addPolygon(a, b, c);
	}
	
	public void addTriangle(Triangle triangle) {
		addTriangle(triangle.a, triangle.b, triangle.c);
	}
	
//	protected class NShape {
//		protected int[] pid;
//		
//		public int[] getID() {
//			return pid;
//		}
//	}
//	
//	protected class Triangle extends NShape {
//		
//		public Triangle(int a, int b, int c) {
//			this.pid = new int[] {a, b, c};
//		}
//	}
//	
//	protected class Quad extends NShape {
//		
//		public Quad(int a, int b, int c, int d) {
//			this.pid = new int[] {a, b, c, d};
//		}
//	}
//	
//	protected class Polygon extends NShape {
//		
//		public Polygon(int... pid) {
//			this.pid = pid;
//		}
//	}
}
