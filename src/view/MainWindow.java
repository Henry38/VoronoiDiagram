package view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import graphic.Viewer2D;
import model.VoronoiModel;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	/** Contucteur */
	public MainWindow() {
		super("Voronoi Diagram");
		
		VoronoiModel model = new VoronoiModel();
		Viewer2D viewer = new Viewer2D(model, 640, 480);
		Viewer2D viewer2 = new Viewer2D(model, 640, 480);
		
		viewer2.removeViewer2DListener(model);
		
		viewer.getCamera().setMoveable(false);
		viewer.getCamera().setSpinnable(false);
		viewer.getCamera().setZoomable(false);
		
		model.setBounds(viewer.getCamera());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewer, viewer2);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(splitPane, BorderLayout.CENTER);
		setContentPane(panel);
		
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public static void main(String[] args) {
		
		Runnable run = new Runnable() {
			public void run() {
				MainWindow fen = new MainWindow();
				fen.setVisible(true);
			}
		};
		
		SwingUtilities.invokeLater(run);
		
	}
}
