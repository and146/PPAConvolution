/**
 * 
 */
package and146.projects.convolution;

import java.util.List;
import java.util.Map;

import and146.projects.convolution.opencv.InMemoryImage;

/**
 * Interface for applying the convolution matrix or matrices on a given image
 * 
 * @author neonards
 *
 */
public interface IConvolutionMatrixProcessor {
	
	/**
	 * Tells what part of convolution process
	 * should be measured in time. Default: WHOLE_PROCESS
	 *
	 */
	public static enum MeasurementType{
		/**
		 * Measures the whole process including
		 * data preparation, border indexes etc.
		 */
		WHOLE_PROCESS,
		
		/**
		 * Measure only the working loops 
		 * (data preparation doesn't interferer with
		 * the measurement).
		 */
		WORKING_LOOPS_ONLY;
	} 
	
	/**
	 * Applies the given convolution matrix on a given file.
	 * 
	 * The output image is not showed in a window.
	 * 
	 * @param image
	 * @param convolutionMatrix
	 * @return Filename of the saved processed (output) image
	 */
	public String process(InMemoryImage image, ConvolutionMatrix convolutionMatrix);
	
	/**
	 * Applies the given convolution matrix on a given file.
	 * 
	 * @param image
	 * @param convolutionMatrix
	 * @param showOutputInWindow
	 * @return Filename of the saved processed (output) image
	 */
	public String process(InMemoryImage image, ConvolutionMatrix convolutionMatrix, boolean showOutputInWindow);
	
	/**
	 * Applies the given convolution matrices on a given file.
	 * 
	 * Output images are not showed in a window.
	 * 
	 * @param image
	 * @param convolutionMatrices
	 * @return List of filenames of the saved processed (output) images
	 */
	public List<String> process(InMemoryImage image, List<ConvolutionMatrix> convolutionMatrices);
	
	/**
	 * Applies the given convolution matrices on a given file.
	 * 
	 * @param image
	 * @param convolutionMatrices
	 * @param showOutputInWindow
	 * @return List of filenames of the saved processed (output) images
	 */
	public List<String> process(InMemoryImage image, List<ConvolutionMatrix> convolutionMatrices, boolean showOutputInWindow);
	
	/**
	 * Returns a map of processing times for each matrix (run process() first).
	 * @return
	 */
	public Map<String, Long> getProcessingTimes();
}
