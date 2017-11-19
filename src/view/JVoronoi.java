package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import listener.VoronoiModelListener;
import math2D.Point2D;
import model.VoronoiModel;
import topology.TopologyContainer;
import viewer2D.graphic.Viewer2D;

public class JVoronoi extends Viewer2D {
	
	private static final long serialVersionUID = 1L;
	public static final String MODEL_CHANGED_PROPERTY = "model";
	
	private VoronoiModel model;
	private Handler handler;
	
	public JVoronoi(VoronoiModel model, int width, int height) {
		super(null, width, height);
		this.model = null;
		this.handler = new Handler();
		
		setModel(model);
		
		getCamera().setMoveable(false);
		getCamera().setSpinnable(false);
		getCamera().setZoomable(false);
		
		this.drawAxis = false;
		this.drawGrid = true;
		
		addMouseListener(handler);
	}
	
	public JVoronoi(VoronoiModel model) {
		this(model, 640, 480);
	}
	
	public VoronoiModel getVoronoiModel() {
		return model;
	}
	
	private Handler getHandler() {
		return handler;
	}
	
	public void setModel(VoronoiModel newModel) {
		VoronoiModel oldModel = getVoronoiModel();
		
		if (oldModel != null) {
			oldModel.removeVoronoiListener(getHandler());
		}
		
		model = newModel;
		
		if (newModel != null) {
			newModel.addVoronoiListener(getHandler());
		}
		
		firePropertyChange(MODEL_CHANGED_PROPERTY, oldModel, model);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		VoronoiModel voronoiModel = getVoronoiModel();
				
		if (voronoiModel != null) {
			int kernelCount = voronoiModel.getKernelsCount();
			Point2D[] kernels = new Point2D[kernelCount];
			
			int n = 0;
			for (Point2D p : voronoiModel.getKernels()) {
				kernels[n] = p;
				n++;
			}
			
			TopologyContainer voronoiTopology = voronoiModel.getVoronoiTopology();
			int polygonCount = voronoiTopology.getPolygonsCount();
			
			// draw Voronoi diagram
			for (int key = 0; key < polygonCount; key++) {
				Point2D kernel = kernels[key];
				Point2D[] polygon = voronoiTopology.getPolygon(key);
				
				int npoints = polygon.length;
				int[] xpoints = new int[npoints];
				int[] ypoints = new int[npoints];
				
				for (int i = 0; i < npoints; i++) {
					Point2D proj_p = screenMVP.transform(polygon[i]);
					xpoints[i] = (int) proj_p.x;
					ypoints[i] = (int) proj_p.y;
				}
				
				Color color = voronoiModel.getColor(kernel);
				g2.setColor(color);
				g2.fillPolygon(xpoints, ypoints, npoints);
			}
			
			TopologyContainer topology = voronoiModel.getDelaunayTopology();
			int triangleCount = topology.getPolygonsCount();
			
			// draw Delaunay triangulation
			for (int key = 0; key < triangleCount; key++) {
				Point2D[] polygon = topology.getPolygon(key);
				
				int npoints = polygon.length;
				int[] xpoints = new int[npoints];
				int[] ypoints = new int[npoints];
				
				for (int i = 0; i < npoints; i++) {
					Point2D proj_p = screenMVP.transform(polygon[i]);
					xpoints[i] = (int) proj_p.x;
					ypoints[i] = (int) proj_p.y;
				}
				
				g2.setColor(Color.black);
				g2.drawPolygon(xpoints, ypoints, npoints);
			}
			
			g2.setColor(Color.black);
			
			// draw kernels
			for (Point2D p : voronoiModel.getKernels()) {
				drawPoint(g2, p);
			}
		}
	}
	
	private class Handler extends MouseAdapter implements VoronoiModelListener {
		
		///
		/// MouseListener
		///
		@Override
		public void mousePressed(MouseEvent ev) {
			if (ev.getButton() == MouseEvent.BUTTON1) {
				double x = ev.getX() + 0.5;
				double y = ev.getY() + 0.5;
				Point2D p = mapToWorld(x, y);
				getVoronoiModel().addKernel(p.x, p.y);
			}
		}
		
		///
		/// VoronoiListener
		///
		@Override
		public void kernelAdded(Point2D p) {
			repaint();
		}
		
		@Override
		public void kernelCleared() {}
	}
}
