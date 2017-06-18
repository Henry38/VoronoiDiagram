package listener;

import java.util.EventListener;

import math2D.Point2D;

public interface VoronoiModelListener extends EventListener {
	public void kernelAdded(Point2D p);
	public void kernelCleared();
}
