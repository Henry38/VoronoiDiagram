package model;

import java.awt.Point;
import java.util.ArrayList;

public class Topology {
	
	private ArrayList<Point> vertex;
	private ArrayList<ArrayList<Integer>> polygons;
	
	/** Constructeur */
	public Topology() {
		this.vertex = new ArrayList<Point>();
		this.polygons = new ArrayList<ArrayList<Integer>>();
	}
	
	public int getVertexCount() {
		return vertex.size();
	}
	
	public int getPolygonsCount() {
		return polygons.size();
	}
	
	public final ArrayList<Point> getVertex() {
		return vertex;
	}
	
	public final ArrayList<Integer> getPolygon(int i) {
		return polygons.get(i);
	}
	
	public void clear() {
		this.vertex.clear();
		this.polygons.clear();
	}
	
	public void addPolygon(Point... points) {
		ArrayList<Integer> polygon = new ArrayList<Integer>();
		for (Point p : points) {
			int index = vertex.indexOf(p);
			if (index == -1) {
				vertex.add(p);
				index = vertex.size() - 1;
			}
			polygon.add(index);
		}
		polygons.add(polygon);
	}
	
	public void addTriangle(Point a, Point b, Point c) {
		addPolygon(a, b, c);
	}
	
	public void addTriangle(Triangle triangle) {
		addTriangle(triangle.a, triangle.b, triangle.c);
	}
}
