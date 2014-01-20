/**
 * 
 */
package and146.projects.convolution.util;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * @author and146
 *
 * Simple stopwatch
 *
 */
public class StopWatch {
	
	private long start;
	private List<Long> stops;
	
	public StopWatch() {
		start = 0;
		stops = new LinkedList<Long>();
	}
	
	/**
	 * Starts the stopwatch
	 */
	public void start() {
		start = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Stops the stopwatch (or adds a split)
	 */
	public void stop() {
		stops.add(Calendar.getInstance().getTimeInMillis());
	}
	
	/**
	 * Resets the stopwatch
	 */
	public void reset() {
		start = Calendar.getInstance().getTimeInMillis();
		stops.clear();
	}
	
	/**
	 * 
	 * @return All splits (see stop()) in ms
	 */
	public List<Long> getAllSplits() {
		
		List<Long> splits = new LinkedList<Long>();
		
		for (Long l : stops) {
			splits.add(l - start);
		}
		
		return splits;
	}
	
	/**
	 * 
	 * @return Time in ms before start() and (last) stop()
	 */
	public Long getTime() {
		if (stops.size() > 0)
			return stops.get(stops.size() - 1) - start;
		else
			return null;
	}
}
