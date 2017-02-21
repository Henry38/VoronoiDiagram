package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

import listener.VoronoiModelListener;
import model.Topology;
import model.VoronoiModel;

public class JVoronoi extends JComponent {

	private static final long serialVersionUID = 1L;
	public static final String MODEL_CHANGED_PROPERTY = "model";
	
	private VoronoiModel model;
	private Handler handler;
	
	/** Constructeur */
	public JVoronoi(VoronoiModel model) {
		super();
		this.model = null;
		this.handler = new Handler();
		
		setModel(model);
		
		addMouseListener(handler);
		setPreferredSize(new Dimension(640, 480));
	}
	
	/** Constructeur */
	public JVoronoi() {
		this(null);
	}
	
	public VoronoiModel getModel() {
		return model;
	}
	
	private Handler getHandler() {
		return handler;
	}
	
	public void setModel(VoronoiModel newModel) {
		VoronoiModel oldModel = getModel();
		
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
		
		g.setColor(new Color(240, 240, 240, 255));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if (getModel() != null) {
			
			Topology topology = getModel().getTopology();
			int polygonCount = topology.getPolygonsCount();
			
			for (int key = 0; key < polygonCount; key++) {
				ArrayList<Point> vertex = topology.getVertex();
				ArrayList<Integer> index = topology.getPolygon(key);
				int npoints = index.size();
				int[] xpoints = new int[npoints];
				int[] ypoints = new int[npoints];
				
				for (int i = 0; i < npoints; i++) {
					xpoints[i] = (int) vertex.get(index.get(i)).getX();
					ypoints[i] = (int) vertex.get(index.get(i)).getY();
				}
				
				//g.setColor(Color.green);
				//g.fillPolygon(xpoints, ypoints, npoints);
				g.setColor(Color.black);
				g.drawPolygon(xpoints, ypoints, npoints);
			}
			
			int k = 1;
			for (Point p : getModel().getKernels()) {
				if (p != null) {
					int x = (int) p.getX();
					int y = (int) p.getY();
					int w = 8;
					int h = 8;
					g.setColor(Color.red);
					g.fillOval(x - w/2, y - h/2, w, h);
					g.setColor(Color.black);
					g.drawString("P"+k, x+8, y+8);
					k++;
				}
			}
		}
	}
	
	
	
	/** Classe qui ecoute le modele et les click utilisateur */
	private class Handler extends MouseAdapter implements VoronoiModelListener {
		
		///
		/// MouseListener
		///
		@Override
		public void mousePressed(MouseEvent ev) {
			if (getModel() != null) {
				int x = ev.getX();
				int y = ev.getY();
				getModel().addKernel(x, y);
			}
		}
		
		///
		/// VoronoiListener
		///
		@Override
		public void kernelAdded(Point p) {
			repaint();
		}
		
		@Override
		public void kernelCleared() {
			repaint();
		}
	}
}
