package model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import data.WorldModel;

import math2D.Point2D;
import math2D.Transformation2D;
import math2D.Vecteur2D;
import topology.Edge;
import topology.TopologyContainer;
import topology.Triangle;
import viewer2D.data.Camera;

public class VoronoiModel extends WorldModel {
	
	
	private Point2D[] bounds;
	private HashMap<Point2D, Color> kernels;
	private TopologyContainer delaunayTopology;
	private TopologyContainer voronoiTopology;
	
	private DelaunayAlgorithm delauneyAlgorithm;
	private VoronoiAlgorithm voronoiAlgorithm;
	
	/** Constructeur */
	public VoronoiModel() {
		this.bounds = new Point2D[4];
		this.kernels = new HashMap<Point2D, Color>();
		this.delaunayTopology = new TopologyContainer();
		this.voronoiTopology = new TopologyContainer();
		this.delauneyAlgorithm = new DelaunayAlgorithm();
		this.voronoiAlgorithm = new VoronoiAlgorithm();
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
		
		fireNeedRefresh();
	}
	
	public void addKernel(Point2D p) {
		addKernel(p.getX(), p.getY());
	}
	
	private void updateDelaunayTriangulation() {
		delaunayTopology.clear();
		
		if (getKernelsCount() < 3) {
			return;
		}
		
		this.delauneyAlgorithm.performed();
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
			Point2D[] kernelArray = new Point2D[2];
			getKernels().toArray(kernelArray);
			
			Point2D a = kernelArray[0];
			Point2D b = kernelArray[1];
			Point2D half_ab = new Point2D((a.x+b.x)/2.0, (a.y+b.y)/2.0);
			double dx = b.getX() - a.getX();
			double dy = b.getY() - a.getY();
			Point2D p1 = rayIntersectionWithBBox(half_ab, new Vecteur2D(-dy, dx));
			Point2D p2 = rayIntersectionWithBBox(half_ab, new Vecteur2D(dy, -dx));
			
			ArrayList<Point2D> boundary = new ArrayList<Point2D>();
			
			boundary.add(p1);
			boundary.add(p2);
			boundary.add(bounds[0]);
			boundary.add(bounds[1]);
			boundary.add(bounds[2]);
			boundary.add(bounds[3]);
			
			// comparateur qui tri les points dans le sens trigonometrique avec l'origine comme centre de rotation
			Comparator<Point2D> counterClockwiseComparator = new Comparator<Point2D>() {
				@Override
				public int compare(Point2D p1, Point2D p2) {
					double radian1 = Math.atan2(p1.getY(), p1.getX());
					double radian2 = Math.atan2(p2.getY(), p2.getX());
					radian1 += (radian1 < 0 ? 2*Math.PI : 0);
					radian2 += (radian2 < 0 ? 2*Math.PI : 0);
					if (Math.abs(radian1) != Math.abs(radian2)) {
						return (radian1 > radian2 ? 1 : -1);
					} else {
						return 0;
					}
				}
			};
			
			Collections.sort(boundary, counterClockwiseComparator);
			
			for (Point2D p : new Point2D[] {p1, p2}) {
				int offset = boundary.indexOf(p);
				ArrayList<Point2D> cellule = new ArrayList<Point2D>(4);
				for (int i = 0; i < boundary.size(); i++) {
					int index = (i + offset) % boundary.size();
					Point2D boundary_p = boundary.get(index);
					cellule.add(boundary_p);
					if ((boundary_p == p2 || boundary_p == p1) && boundary_p != p) {
						i = boundary.size();
					}
				}
				
				int npoints = cellule.size();
				Point2D[] celluleArray = new Point2D[npoints];
				cellule.toArray(celluleArray);
				voronoiTopology.addPolygon(celluleArray);
			}
			
			return;
		}
		
		this.voronoiAlgorithm.performed();
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
	
	@Override
	public void pointPressed(double x, double y) {
		addKernel(x, y);
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
			
			for (Point2D kernel : kernels.keySet()) {
				run(kernel);
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
				triangles.add(new Triangle(edge.a, edge.b, kernel));
			}
		}
		
		private void prune() {
			
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
	
	/** Private class */
	private class VoronoiAlgorithm {
		
		private TopologyContainer topology;
		private int triangleCount;
		
		/** Constructeur */
		public VoronoiAlgorithm() {
			
		}
		
		public void performed() {
			init();
			
			// performed a Voronoi diagram
			for (Point2D kernel : getKernels()) {
				run(kernel);
			}
		}
		
		private void init() {
			topology = getDelaunayTopology();
			triangleCount = topology.getPolygonsCount();
		}
		
		private void run(Point2D kernel) {
			// parcourir la triangulation de Delaunay et etablir
			// la liste des triangles adjacent a kernel
			ArrayList<Triangle> listTriangles = new ArrayList<Triangle>();
			for (int key = 0; key < triangleCount; key++) {
				Point2D[] polygon = topology.getPolygon(key);
				for (int i = 0; i < 3; i++) {
					if (polygon[i] == kernel) {
						Point2D a = polygon[i];
						Point2D b = polygon[(i+1) % 3];
						Point2D c = polygon[(i+2) % 3];
						Triangle triangle = new Triangle(a, b, c);
						triangle.orient();
						listTriangles.add(triangle);
					}
				}
			}
			
			int npoints = listTriangles.size();
			
			// comparateur qui tri les points dans le sens trigonometrique avec kernel comme centre de rotation
			Comparator<Point2D> counterClockwiseComparator = new Comparator<Point2D>() {
				@Override
				public int compare(Point2D p1, Point2D p2) {
					double radian1 = Math.atan2(p1.getY() - kernel.getY(), p1.getX() - kernel.getX());
					double radian2 = Math.atan2(p2.getY() - kernel.getY(), p2.getX() - kernel.getX());
					radian1 += (radian1 < 0 ? 2*Math.PI : 0);
					radian2 += (radian2 < 0 ? 2*Math.PI : 0);
					if (Math.abs(radian1) != Math.abs(radian2)) {
						return (radian1 > radian2 ? 1 : -1);
					} else {
						return 0;
					}
				}
			};
			
			// liste dans le sens trigonometrique les triangles adjacent a kernel
			TreeMap<Point2D, Triangle> treeMap = new TreeMap<Point2D, Triangle>(counterClockwiseComparator); 
			for (int i = 0; i < npoints; i++) {
				Triangle triangle = listTriangles.get(i);
				Point2D circumCenter = triangle.getCircumCenter();
				treeMap.put(circumCenter, triangle);
			}
			
			ArrayList<Point2D> circumCenterToAdd = new ArrayList<Point2D>();
			
			// calcule la liste des points qui complete une cellule non bornee 
			for(Entry<Point2D, Triangle> entry : treeMap.entrySet()) {
				Entry<Point2D, Triangle> nextEntry = treeMap.higherEntry(entry.getKey());
				if (nextEntry == null) {
					nextEntry = treeMap.firstEntry();
				}
				Triangle t1 = entry.getValue();
				Triangle t2 = nextEntry.getValue();
				if (t1.getC() != t2.getB()) {
					Point2D p1 = bBoxIntersection(kernel, entry, false);
					Point2D p2 = bBoxIntersection(kernel, nextEntry, true);
					
					ArrayList<Point2D> boundary = new ArrayList<Point2D>();
					
					boundary.add(p1);
					boundary.add(p2);
					boundary.add(bounds[0]);
					boundary.add(bounds[1]);
					boundary.add(bounds[2]);
					boundary.add(bounds[3]);
					
					Collections.sort(boundary, counterClockwiseComparator);
					
					int offset = boundary.indexOf(p1);
					for (int i = 0; i < boundary.size(); i++) {
						int index = (i + offset) % boundary.size();
						Point2D p = boundary.get(index);
						circumCenterToAdd.add(p);
						if (p == p2) {
							i = boundary.size();
						}
					}
				}
			}
			
			// ajoute les points pour borner la cellule
			npoints += circumCenterToAdd.size();
			for (Point2D p : circumCenterToAdd) {
				treeMap.put(p, null);
			}
			
			// ajoute le polygone representant la cellule a la topology de voronoi
			Point2D[] circumCenterArray = new Point2D[npoints];
			treeMap.keySet().toArray(circumCenterArray);
			voronoiTopology.addPolygon(circumCenterArray);
		}
		
		private Point2D bBoxIntersection(Point2D kernel, Entry<Point2D, Triangle> entry, boolean reverse) {
			Triangle triangle = entry.getValue();
			Point2D circumCenter = entry.getKey();
			
			triangle.orient();
			
			int offset = (reverse ? 1 : 2);
			int rx = (reverse ? 1 : -1);
			int ry = (reverse ? -1 : 1);
			
			Point2D[] points = triangle.getVertex();
			for (int i = 0; i < 3; i++) {
				if (points[i] == kernel) {
					Vecteur2D vect = new Vecteur2D(kernel, points[(i+offset) % 3]);
					vect.normalize();
					// rotation the normale by +/-90 degrees 
					double dx = rx * Math.round(vect.getDy() * 1000) / 1000.0;
					double dy = ry * Math.round(vect.getDx() * 1000) / 1000.0;
					return rayIntersectionWithBBox(circumCenter, new Vecteur2D(dx, dy));
				}
			}
			
			return null;
		}
	}
}
