package model;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import listener.VoronoiModelListener;

public class VoronoiModel {
	
	private EventListenerList listenerList;
	private HashMap<Point, Color> kernels;
	private Topology delaunayTopology;
	private Topology voronoiTopology;
	
	private DelaunayTriangulation dTriangulation;
	
	/** Constructeur */
	public VoronoiModel() {
		this.listenerList = new EventListenerList();
		this.kernels = new HashMap<Point, Color>();
		this.delaunayTopology = new Topology();
		this.voronoiTopology = new Topology();
		this.dTriangulation = new DelaunayTriangulation();
		//addKernel(396, 297);
		//addKernel(273, 105);
		//addKernel(94,234);
	}
	
	public int getKernelsCount() {
		return kernels.size();
	}
	
	public final Set<Point> getKernels() {
		return kernels.keySet();
	}
	
	public final Color getColor(Point key) {
		if (key == null) {
			return Color.black;
		}
		return kernels.get(key);
	}
	
	public final Topology getDelaunayTopology() {
		return delaunayTopology;
	}
	
	public final Topology getVoronoiTopology() {
		return voronoiTopology;
	}
	
	public void clearAll() {
		this.kernels.clear();
		this.delaunayTopology.clear();
		this.voronoiTopology.clear();
		fireKernelCleared();
	}
	
	public void addKernel(int x, int y) {
		int r = (int) (Math.random() * 255.0);
		int g = (int) (Math.random() * 255.0);
		int b = (int) (Math.random() * 255.0);
		Point key = new Point(x, y);
		Color value = new Color(r, g, b);
		kernels.put(key, value);
		
		delaunayTriangulation();
		//voronoiDiagram();
		
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
	
	private void fireKernelAdded(Point p) {
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
	
	private void delaunayTriangulation() {
		delaunayTopology.clear();
		
		if (getKernelsCount() < 3) {
			return;
		}
		
		this.dTriangulation.performed();
	}
	
	private void voronoiDiagram() {
		if (getKernelsCount() == 1) {
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
	private class DelaunayTriangulation {
		
		public ArrayList<Triangle> triangles;
		public HashSet<Triangle> badTriangles;
		public HashSet<Edge> polygon;
		public Point p1, p2, p3;
		
		/** Constructeur */
		public DelaunayTriangulation() {
			this.triangles = new ArrayList<Triangle>();
			this.badTriangles = new HashSet<Triangle>();
			this.polygon = new HashSet<Edge>();
			this.p1 = new Point();
			this.p2 = new Point();
			this.p3 = new Point();
		}
		
		public void performed() {
			init();
			
			for (Point p : kernels.keySet()) {
				run(p);
			}
			
			prune();
		}
		
		public void init() {
			
			// calcule un triangle englobant
			Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
			Point max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
			
			for (Point p : kernels.keySet()) {
				min.x = (p.x < min.x ? p.x : min.x);
				min.y = (p.y < min.y ? p.y : min.y);
				max.x = (p.x > max.x ? p.x : max.x);
				max.y = (p.y > max.y ? p.y : max.y);
			}
			
			int dx = max.x - min.x;
			int dy = max.y - min.y;
			
			p1.setLocation(min.x - dx, min.y - dy);
			p2.setLocation(min.x - dx + 4 * dx, min.y - dy);
			p3.setLocation(min.x - dx, min.y - dy + 4 * dy);
			
			// triangluation de Delaunay initiale
			triangles.clear();
			triangles.add(new Triangle(p1, p2, p3));
			
			badTriangles.clear();
			polygon.clear();
		}
		
		public void run(Point p) {
			
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
			
			// supprime tous les triangles qui ont un sommet en communt avec le triangle initial
			for (int i = triangles.size()-1; i >= 0; i--) {
				Triangle triangle = triangles.get(i);
				boolean isRemoved = false;
				
				for (Point p : triangle.getVertex()) {
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
