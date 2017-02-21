package listener;

import java.awt.Point;
import java.util.EventListener;

public interface VoronoiModelListener extends EventListener {
	public void kernelAdded(Point p);
	public void kernelCleared();
}
