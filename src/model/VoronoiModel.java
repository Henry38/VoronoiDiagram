package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import listener.VoronoiModelListener;
import math2D.Point2D;
import topology.Edge;
import topology.TopologyContainer;
import topology.Triangle;

public class VoronoiModel {
	
	private EventListenerList listenerList;
	private HashMap<Point2D, Color> kernels;
	private TopologyContainer delaunayTopology;
	private TopologyContainer voronoiTopology;
	
	private DelaunayAlgorithm algorithm;
	
	/** Constructeur */
	public VoronoiModel() {
		this.listenerList = new EventListenerList();
		this.kernels = new HashMap<Point2D, Color>();
		this.delaunayTopology = new TopologyContainer();
		this.voronoiTopology = new TopologyContainer();
		this.algorithm = new DelaunayAlgorithm();
		
		//addKernel(353, 424);
		//addKernel(86, 312);
		//addKernel(163, 168);
	}
	
	public int getKernelsCount() {
		return kernels.size();
	}
	
	public final Set<Point2D> getKernels() {
		return kernels.keySet();
	}
	
	public final Color getColor(Point2D key) {
		if (key == null) {
			return Color.black;
		}
		return kernels.get(key);
	}
	
	public final TopologyContainer getDelaunayTopology() {
		return delaunayTopology;
	}
	
	public final TopologyContainer getVoronoiTopology() {
		return voronoiTopology;
	}
	
	public void clearAll() {
		this.kernels.clear();
		this.delaunayTopology.clear();
		this.voronoiTopology.clear();
		fireKernelCleared();
	}
	
	public void addKernel(double x, double y) {
		int r = (int) (Math.random() * 255.0);
		int g = (int) (Math.random() * 255.0);
		int b = (int) (Math.random() * 255.0);
		Point2D key = new Point2D(x, y);
		Color value = new Color(r, g, b);
		kernels.put(key, value);
		
		updateDelaunayTriangulation();
		updateVoronoiDiagram();
		
		fireKernelAdded(key);
	}
	
	/** Ajoute un listener sur le modele */
	public void addVoronoiListener(VoronoiModelListener l) {
		listenerList.add(VoronoiModelListener.class, l);
	}
	
	/** Retire un listener sur le modele */
	public void removeVoronoiListener(VoronoiModelListener l) {
		listenerList.remove(VoronoiModelListener.class, l);
	}
	
	private void fireKernelAdded(Point2D p) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] instanceof VoronoiModelListener) {
				((VoronoiModelListener) listeners[i]).kernelAdded(p);
			}
		}
	}
	
	private void fireKernelCleared() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] instanceof VoronoiModelListener) {
				((VoronoiModelListener) listeners[i]).kernelCleared();
			}
		}
	}
	
	private void updateDelaunayTriangulation() {
		delaunayTopology.clear();
		
		if (getKernelsCount() < 3) {
			return;
		}
		
		this.algorithm.performed();
	}
	
	private void updateVoronoiDiagram() {
		voronoiTopology.clear();
		
		if (getKernelsCount() == 0) {
			return;
		}
		
		if (getKernelsCount() == 1) {
			Point2D c1 = new Point2D(0, 0);
			Point2D c2 = new Point2D(640, 0);
			Point2D c3 = new Point2D(640, 480);
			Point2D c4 = new Point2D(0, 480);
			voronoiTopology.addPolygon(c1, c2, c3, c4);
			return;
		}
		
		if (getKernelsCount() == 2) {
			return;
		}
		
//		for (Point p : getKernels())  {
//			ArrayList<Triangle> a = new ArrayList<Triangle>();
//			int polygonCount = delaunayTopology.getPolygonsCount();
//			for (int i = 0; i < polygonCount; i++) {
//				if (triangle.contains(p)) {
//					
//				}
//			}
//		}
	}
	
	/** Private class */
	private class DelaunayAlgorithm {
		
		public ArrayList<Triangle> triangles;
		public HashSet<Triangle> badTriangles;
		public HashSet<Edge> polygon;
		public Point2D p1, p2, p3;
		
		/** Constructeur */
		public DelaunayAlgorithm() {
			this.triangles = new ArrayList<Triangle>();
			this.badTriangles = new HashSet<Triangle>();
			this.polygon = new HashSet<Edge>();
			this.p1 = new Point2D();
			this.p2 = new Point2D();
			this.p3 = new Point2D();
		}
		
		public void performed() {
			init();
			
			for (Point2D p : kernels.keySet()) {
				run(p);
			}
			
			prune();
		}
		
		public void init() {
			
			// calcule un triangle englobant
			Point2D min = new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
			Point2D max = new Point2D(Integer.MIN_VALUE, Integer.MIN_VALUE);
			
			for (Point2D p : kernels.keySet()) {
				min.x = (p.x < min.x ? p.x : min.x);
				min.y = (p.y < min.y ? p.y : min.y);
				max.x = (p.x > max.x ? p.x : max.x);
				max.y = (p.y > max.y ? p.y : max.y);
			}
			
			double dx = max.x - min.x;
			double dy = max.y - min.y;
			
			p1.set(min.x - dx, min.y - dy);
			p2.set(min.x - dx + 4 * dx, min.y - dy);
			p3.set(min.x - dx, min.y - dy + 4 * dy);
			
			// triangluation de Delaunay initiale
			triangles.clear();
			triangles.add(new Triangle(p1, p2, p3));
			
			badTriangles.clear();
			polygon.clear();
		}
		
		public void run(Point2D p) {
			
			badTriangles.clear();
			polygon.clear();
			
			// suppime tous les triangles ne respectant la condition de Delaunay
			for (int i = triangles.size()-1; i >= 0; i--) {
				Triangle triangle = triangles.get(i);
				if (triangle.inCircum(p)) {
					badTriangles.add(triangle);
					triangles.remove(i);
				}
			}
			
			// ajoute les aretes au polygon englobant
			for (Triangle triangle : badTriangles) {
				for (Edge edge : triangle.getEdges()) {
					if (!polygon.add(edge)) {
						polygon.remove(edge);
					}
				}
			}
			
			// cree de nouveaux triangles en utilisant le sommet courant et le polygone englobant
			for (Edge edge: polygon) {
				triangles.add(new Triangle(edge.a, edge.b, p));
			}
		}
		
		public void prune() {
			
			// supprime tous les triangles qui ont un sommet en commun avec le triangle initial
			for (int i = triangles.size()-1; i >= 0; i--) {
				Triangle triangle = triangles.get(i);
				boolean isRemoved = false;
				
				for (Point2D p : triangle.getVertex()) {
					isRemoved |= (p.equals(p1) || p.equals(p2) || p.equals(p3));
				}
				
				if (isRemoved) {
					triangles.remove(i);
				}
			}
			
			// cree la triangulation de Delaunay
			for (Triangle triangle : triangles) {
				delaunayTopology.addTriangle(triangle);
			}
		}
	}
}
