package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;

import listener.VoronoiModelListener;
import model.Topology;
import model.VoronoiModel;

public class JVoronoi extends JComponent {
	
	private static final long serialVersionUID = 1L;
	public static final String MODEL_CHANGED_PROPERTY = "model";
	
	private VoronoiModel model;
	private Handler handler;
	
	private JButton clear;
	private JButton next;
	private int currentTriangle;
	
	/** Constructeur */
	public JVoronoi(VoronoiModel model) {
		super();
		this.model = null;
		this.handler = new Handler();
		
		this.clear = new JButton("Clear");
		this.clear.setLocation(16, 16);
		this.clear.setSize(128, 32);
		this.clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getModel() != null) {
					getModel().clearAll();
				}
			}
		});
		
		this.next = new JButton("Next");
		this.next.setLocation(144, 16);
		this.next.setSize(128, 32);
		this.next.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getModel() != null) {
					Topology topology = getModel().getDelaunayTopology();
					int polygonCount = topology.getPolygonsCount();
					if (polygonCount > 0) {
						currentTriangle = (currentTriangle + 1) % polygonCount;
						repaint();
					}
				}
			}
		});
		this.currentTriangle = -1;
		
		this.add(clear);
		this.add(next);
		
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
			
			Topology topology = getModel().getDelaunayTopology();
			int polygonCount = topology.getPolygonsCount();
			
			for (int key = 0; key < polygonCount; key++) {
				ArrayList<Point> vertex = topology.getVertex();
				ArrayList<Integer> index = topology.getPolygon(key);
				int npoints = index.size();
				int[] xpoints = new int[npoints];
				int[] ypoints = new int[npoints];
				
				for (int i = 0; i < npoints; i++) {
					xpoints[i] = (int) vertex.get(index.get(i)).x;
					ypoints[i] = (int) vertex.get(index.get(i)).y;
				}
				
				//g.setColor(Color.green);
				//g.fillPolygon(xpoints, ypoints, npoints);
				g.setColor(Color.black);
				g.drawPolygon(xpoints, ypoints, npoints);
			}
			
			for (Point p : getModel().getKernels()) {
				if (p != null) {
					int x = (int) p.x;
					int y = (int) p.y;
					int w = 8;
					int h = 8;
					g.setColor(Color.black);
					g.fillOval(x - w/2, y - h/2, w, h);
				}
			}

			if (this.currentTriangle != -1) {
				ArrayList<Point> vertex = topology.getVertex();
				ArrayList<Integer> index = topology.getPolygon(currentTriangle);
				
				int npoints = index.size();
				int[] xpoints = new int[npoints];
				int[] ypoints = new int[npoints];
				
				for (int i = 0; i < npoints; i++) {
					xpoints[i] = (int) vertex.get(index.get(i)).getX();
					ypoints[i] = (int) vertex.get(index.get(i)).getY();
				}
				
				g.setColor(Color.blue);
				g.drawPolygon(xpoints, ypoints, npoints);
				
				Point a = new Point(xpoints[0], ypoints[0]);
				Point b = new Point(xpoints[1], ypoints[1]);
				Point c = new Point(xpoints[2], ypoints[2]);
				Point ab = new Point(b.x - a.x, b.y - a.y);
				double area = ((b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x)) / 2.0;
				if (area < 0) {
					Point tmp = b;
					b = c;
					c = tmp;
					area = -area;
				}
				double d_ab = a.distance(b);
				double d_bc = b.distance(c);
				double d_ca = c.distance(a);
				double R = (d_ab * d_bc * d_ca) / (4 * area);
				
				double dist = R * Math.sin( Math.acos((d_ab/2.0) / R) );
				Point2D n_ab = new Point2D.Double(-ab.y / d_ab, ab.x / d_ab);
				double radian_c = (b.x-c.x)*(a.x-c.x) + (b.y-c.y)*(a.y-c.y);
				if (radian_c < 0) {
					dist = -dist;
				}
				
				int cx = (int) (((a.x + b.x) / 2.0) + (dist * n_ab.getX()));
				int cy = (int) (((a.y + b.y) / 2.0) + (dist * n_ab.getY()));
				
				g.setColor(Color.red);
				g.fillOval(cx-2, cy-2, 4, 4);
				g.drawOval(cx-(int)R, cy-(int)R, (int)R*2, (int)R*2);
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
			currentTriangle = -1;
			repaint();
		}
		
		@Override
		public void kernelCleared() {
			currentTriangle = -1;
			repaint();
		}
	}
}
