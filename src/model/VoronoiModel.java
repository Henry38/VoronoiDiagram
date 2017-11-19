package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import listener.VoronoiModelListener;
import math2D.Point2D;
import math2D.Vecteur2D;
import topology.Edge;
import topology.TopologyContainer;
import topology.Triangle;
import viewer2D.data.Camera;

public class VoronoiModel {
	
	private EventListenerList listenerList;
	
	private Point2D[] bounds;
	private HashMap<Point2D, Color> kernels;
	private TopologyContainer delaunayTopology;
	private TopologyContainer voronoiTopology;
	
	private DelaunayAlgorithm algorithm;
	
	/** Constructeur */
	public VoronoiModel() {
		this.listenerList = new EventListenerList();
		this.bounds = new Point2D[4];
		this.kernels = new HashMap<Point2D, Color>();
		this.delaunayTopology = new TopologyContainer();
		this.voronoiTopology = new TopologyContainer();
		this.algorithm = new DelaunayAlgorithm();
	}
	
	public int getKernelsCount() {
		return kernels.size();
	}
	
	public Set<Point2D> getKernels() {
		return kernels.keySet();
	}
	
	public Color getColor(Point2D key) {
		if (key == null) {
			return Color.black;
		}
		return kernels.get(key);
	}
	
	public TopologyContainer getDelaunayTopology() {
		return delaunayTopology;
	}
	
	public TopologyContainer getVoronoiTopology() {
		return voronoiTopology;
	}
	
	public void setBounds(Camera camera) {
		Rectangle2D.Double rect = camera.getRectangle();
		
		double left = rect.getMinX();
		double bottom = rect.getMinY();
		double right = rect.getMaxX();
		double top = rect.getMaxY();
		
		Transformation2D inverseView = camera.viewMat().getInverseTransformation();
		
		// Calcul des quatres points du rectangle (repere monde)
		this.bounds[0] = inverseView.transform(new Point2D(left, bottom));
		this.bounds[1] = inverseView.transform(new Point2D(right, bottom));
		this.bounds[2] = inverseView.transform(new Point2D(right, top));
		this.bounds[3] = inverseView.transform(new Point2D(left, top));
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
		Color value = new Color(r, g, b, 204);
		kernels.put(key, value);
		
		updateDelaunayTriangulation();
		updateVoronoiDiagram();
		
		fireKernelAdded(key);
	}
	
	public void addKernel(Point2D p) {
		addKernel(p.getX(), p.getY());
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
			voronoiTopology.addPolygon(this.bounds);
			return;
		}
		
		if (getKernelsCount() == 2) {
			return;
		}
		
	private Point2D rayIntersectionWithBBox(Point2D origin, Vecteur2D direction) {
		double xmin = bounds[0].getX();
		double ymin = bounds[0].getY();
		double xmax = bounds[2].getX();
		double ymax = bounds[2].getY();
		
		double x = origin.getX();
		double y = origin.getY();
		
		double dx = direction.getDx();
		double dy = direction.getDy();
		
		double t0x = (dx != 0 ? (xmin - x) / dx : 0);
		double t1x = (dx != 0 ? (xmax - x) / dx : 0);
		double t0y = (dy != 0 ? (ymin - y) / dy : 0);
		double t1y = (dy != 0 ? (ymax - y) / dy : 0);
		
		double tmin = Double.MAX_VALUE;
		for (double t : new double[] {t0x, t0y, t1x, t1y}) {
			tmin = (t > 0 && t < tmin ? t : tmin);
		}
		
		return new Point2D(x + tmin * dx, y + tmin * dy);
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
		
		private void init() {
			
			// calcule un triangle englobant
			Point2D min = new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
			Point2D max = new Point2D(Integer.MIN_VALUE, Integer.MIN_VALUE);
			
			for (Point2D p : getKernels()) {
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
		
		private void run(Point2D kernel) {
			
			badTriangles.clear();
			polygon.clear();
			
			// suppime tous les triangles ne respectant la condition de Delaunay
			for (int i = triangles.size()-1; i >= 0; i--) {
				Triangle triangle = triangles.get(i);
				if (triangle.inCircum(kernel)) {
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
