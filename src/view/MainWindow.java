package view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.VoronoiModel;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	/** Contucteur */
	public MainWindow() {
		super("Voronoi Diagram");
		
		VoronoiModel model = new VoronoiModel();
		JVoronoi jvoronoi = new JVoronoi(model);
		
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
