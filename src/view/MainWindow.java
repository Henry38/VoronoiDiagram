package view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import graphic.Viewer2D;
import model.VoronoiModel;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	/** Contucteur */
	public MainWindow() {
		super("Voronoi Diagram");
		
		VoronoiModel model = new VoronoiModel();
		Viewer2D jvoronoi = new Viewer2D(model, 640, 480);
		
		jvoronoi.getCamera().setMoveable(false);
		jvoronoi.getCamera().setSpinnable(false);
		jvoronoi.getCamera().setZoomable(false);
		
		model.setBounds(jvoronoi.getCamera());
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(jvoronoi);
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
