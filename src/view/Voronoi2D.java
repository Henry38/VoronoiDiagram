package view;

import java.awt.Color;
import java.awt.Graphics2D;

import geometry.Topology2D;
import graphic.Viewer2D.DrawTool;
import math2D.Point2D;
import primitive.Edge;
import primitive.Face;
import primitive.Quad;
import primitive.Triangle;
import topology.BaseMeshTopology;

public class Voronoi2D<T extends Point2D> extends Topology2D<T> {
	
	/** Constructeur */
	public Voronoi2D(BaseMeshTopology<T> topology) {
		super(topology);
	}
	
	private void setRandomColor(Graphics2D g2) {
		int r = (int) (Math.random() * 255.0);
		int g = (int) (Math.random() * 255.0);
		int b = (int) (Math.random() * 255.0);
		Color c = new Color(r, g, b, 128);
		g2.setColor(c);
	}
	
	@Override
	public void draw(Graphics2D g2, DrawTool drawTool) {
		Point2D[] points;
		int npoints = 0;
		
		g2.setColor(Color.black);
		
		for (Edge edge : topology.getEdges()) {
			Point2D p1 = topology.getPosition(edge.a);
			Point2D p2 = topology.getPosition(edge.b);
			drawTool.drawLine(g2, p1, p2);
		}
		
		for (Triangle triangle : topology.getTriangles()) {
			Point2D p1 = topology.getPosition(triangle.a);
			Point2D p2 = topology.getPosition(triangle.b);
			Point2D p3 = topology.getPosition(triangle.c);
			setRandomColor(g2);
			drawTool.fillPolygon(g2, p1, p2, p3);
		}
		
		for (Quad quad : topology.getQuads()) {
			Point2D p1 = topology.getPosition(quad.a);
			Point2D p2 = topology.getPosition(quad.b);
			Point2D p3 = topology.getPosition(quad.c);
			Point2D p4 = topology.getPosition(quad.d);
			setRandomColor(g2);
			drawTool.fillPolygon(g2, p1, p2, p3, p4);
		}
		
		for (Face face : topology.getFaces()) {
			npoints = face.getNbPoints();
			points = new Point2D[npoints];
			for (int i = 0; i < npoints; i++) {
				points[i] = topology.getPosition(face.get(i));
			}
			setRandomColor(g2);
			drawTool.fillPolygon(g2, points);
		}
	}
}
