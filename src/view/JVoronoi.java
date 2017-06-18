package view;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import listener.VoronoiModelListener;
import math2D.Point2D;
import model.VoronoiModel;
import topology.TopologyContainer;
import viewer2D.geometry.Shape2D;
import viewer2D.graphic.Viewer2D;

public class JVoronoi extends Viewer2D {
	
	private static final long serialVersionUID = 1L;
	public static final String MODEL_CHANGED_PROPERTY = "model";
	
	private VoronoiModel model;
	private Handler handler;
	
	public JVoronoi(VoronoiModel model, int width, int height) {
		super(width, height);
		this.model = null;
		this.handler = new Handler();
		
		setModel(model);
		
//		getCamera().setMoveable(false);
//		getCamera().setSpinnable(false);
//		getCamera().setZoomable(false);
		
		this.drawAxis = true;
		this.drawGrid = false;
		
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
	
	private class Handler extends MouseAdapter implements VoronoiModelListener {
		
		///
		/// MouseListener
		///
		@Override
		public void mousePressed(MouseEvent ev) {
			if (ev.getButton() == MouseEvent.BUTTON3) {
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
			// update viewer2D
			getModel().removeAll();
			TopologyContainer t = getVoronoiModel().getDelaunayTopology();
			for (int i = 0; i < t.getPolygonsCount(); i++) {
				Point2D[] polygon = t.getPolygon(i);
				Shape2D shape = new Shape2D(polygon);
				shape.setColor(Color.black);
				shape.setWireframe(true);
				getModel().add(shape);
				repaint();
			}
		}
		
		@Override
		public void kernelCleared() {}
	}
}
