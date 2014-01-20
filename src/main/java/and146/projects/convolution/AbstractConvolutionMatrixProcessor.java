/**
 * 
 */
package and146.projects.convolution;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import and146.projects.convolution.gui.ImageFrame;
import and146.projects.convolution.opencv.InMemoryGrayscaleImage;
import and146.projects.convolution.opencv.InMemoryImage;
import and146.projects.convolution.util.StopWatch;

/**
 * Abstract implementation of IConvolutionMatrixProcessor
 * 
 * @author neonards
 *
 */
public abstract class AbstractConvolutionMatrixProcessor implements IConvolutionMatrixProcessor {

	private Logger log = LoggerFactory.getLogger(AbstractConvolutionMatrixProcessor.class);
	
	protected static final MeasurementType DEFAULT_MEASUREMENT_TYPE = MeasurementType.WHOLE_PROCESS; 
	
	private static final Map<Class<? extends IConvolutionMatrixProcessor>, Map<String, Long>> times = new HashMap<Class<? extends IConvolutionMatrixProcessor>, Map<String, Long>>();
	
	private MeasurementType measurementType;
	private StopWatch tmpIterStopWatch;
	
	public AbstractConvolutionMatrixProcessor() {
		this(DEFAULT_MEASUREMENT_TYPE);
	}
	
	public AbstractConvolutionMatrixProcessor(MeasurementType measurementType) {
		this.measurementType = measurementType;
	}
	
	@Override
	public synchronized final String process(InMemoryImage image,
			ConvolutionMatrix convolutionMatrix) {
		return process(image, convolutionMatrix, false);
	}
	
	@Override
	public synchronized final String process(InMemoryImage image,
			ConvolutionMatrix convolutionMatrix, boolean showOutputInWindow) {
		List<ConvolutionMatrix> matrices = new ArrayList<ConvolutionMatrix>();
		matrices.add(convolutionMatrix);
		
		return process(image, matrices, showOutputInWindow).get(0);
	}
	
	@Override
	public synchronized final List<String> process(InMemoryImage image,
			List<ConvolutionMatrix> convolutionMatrices) {
		
		return process(image, convolutionMatrices, false);
	}
	
	@Override
	public synchronized final List<String> process(InMemoryImage image,
			List<ConvolutionMatrix> convolutionMatrices,
			boolean showOutputInWindow) {
		
		List<String> outputImages = new LinkedList<String>();
		
		// start the stop watch
		StopWatch swTotal = new StopWatch();
		Map<String, Long> timesPerImpl = new HashMap<String,Long>();
		swTotal.start();
		
		for (ConvolutionMatrix matrix : convolutionMatrices) {
			System.out.println();
			log.info(String.format("Processing with matrix '%s' ...", matrix.getName()));
			
			StopWatch swIter = new StopWatch();
			tmpIterStopWatch = swIter;
			
			int[][] convolutionMatrix = matrix.getMatrix();
			
			// clone the input image a process it
			Mat resultImage = image.getMat().clone();
			swIter.start();	// start the stop watch
			processImage(image.getMat(), resultImage, matrix);
			swIter.stop();  // stop the stop watch
			
			// write the output image to a file and show it
			File newFile = new File(image.getFilePath());
			String newFileName = matrix.getName() + "__" + getImplementationSuffix() + "__" + newFile.getName();
			log.info(String.format("Creating new output file: '%s'", newFileName));
			outputImages.add(newFileName);
			Highgui.imwrite(newFileName, resultImage);
			InMemoryImage outputImage = new InMemoryGrayscaleImage(newFileName);
			if (showOutputInWindow) {
				ImageFrame outputImageFrame = new ImageFrame(outputImage);
			}
			
			timesPerImpl.put(matrix.getName(), swIter.getAllSplits().get(0));
			log.info(String.format("Processing done. Time: %d s", swIter.getAllSplits().get(0) / 1000));
			tmpIterStopWatch = null;
		}
		
		// stop the stop watch and print the total processing time
		swTotal.stop();
		if (convolutionMatrices.size() > 1)
			log.info(String.format("Total processing time: %d s.", swTotal.getTime() / 1000));
		
		times.put(getClass(), timesPerImpl);
		
		return outputImages;
	}
	
	/**
	 * Applies the convolution matrix on a given input image 
	 * and saves the result to result image 
	 * 
	 * @param inputImage The input image
	 * @param resultImage The result image, that will be modified based on the matrix
	 * @param matrix The convolution matrix
	 */
	protected abstract void processImage(Mat inputImage, Mat resultImage, ConvolutionMatrix matrix);
	
	/**
	 * 
	 * @return String suffix, that will be appended to the result filename - should be unique for each implemenation!
	 */
	protected String getImplementationSuffix() {
		return getClass().getSimpleName();
	};
	
	/**
	 * Updates the Command line progress bar with a given value
	 * 
	 * @param newValue
	 */
	protected void updateCliProgressBar(int newValue) {
		System.out.print(String.format("[Processing... %d%% ]\r", newValue));
	}
	
	@Override
	public Map<String, Long> getProcessingTimes() {
		return times.get(getClass());
	}
	
	protected final void onWorkingLoopStart() {
		if (measurementType.equals(MeasurementType.WORKING_LOOPS_ONLY))
			tmpIterStopWatch.reset();
	}
	
	protected final void onWorkingLoopEnd() {
		if (measurementType.equals(MeasurementType.WORKING_LOOPS_ONLY))
			tmpIterStopWatch.stop();
	}
}
