/**
 * 
 */
package and146.projects.convolution;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serial implementation of IConvolutionMatrixProcessor
 * 
 * @author neonards
 *
 */
public class ConvolutionMatrixProcessorSerialImpl extends AbstractConvolutionMatrixProcessor{

	private Logger log = LoggerFactory.getLogger(ConvolutionMatrixProcessorSerialImpl.class);
	
	public ConvolutionMatrixProcessorSerialImpl() {
		super();
	}
	
	public ConvolutionMatrixProcessorSerialImpl(MeasurementType measurementType) {
		super(measurementType);
	}
	
	@Override
	protected void processImage(Mat inputImage, Mat resultImage,
			ConvolutionMatrix matrix) {
		
		int[][] convolutionMatrix = matrix.getMatrix();
		
		// matrix indexes for coords (x=0/y=1)
		final int x = 0;
		final int y = 1;
		// calculate convolution matrix's center indexes (this is why we expect the odd-sized matrices)
		int[] matrixCenter = new int[2];
		matrixCenter[x] = (convolutionMatrix.length) / 2; // x
		matrixCenter[y] = (convolutionMatrix[0].length) / 2; // y
					
		// calculate convolution matrix's border indexes
		int[] matrixBorder = new int[2];
		matrixBorder[x] = convolutionMatrix.length - 1; // x
		matrixBorder[y] = convolutionMatrix[0].length - 1; // y
					
		// process the input image and save the result to the result image
		int beginRowIndex = matrixCenter[x];
		int endRowIndex = inputImage.rows() - 1 - matrixCenter[x];
		int beginColIndex = matrixCenter[y];
		int endColIndex = inputImage.cols() - 1 - matrixCenter[y];
		
		onWorkingLoopStart();
		for (int row = beginRowIndex; row <= endRowIndex; row++) {
			// show progress
			updateCliProgressBar(100 * row / endRowIndex);
			
			for (int col = beginColIndex; col <= endColIndex; col++) {
				// [row][col] is the place where we put the center of convolution matrix (and coords of the result pixel)
				
				double newValue = 0.0;
				
				// let's apply
				for (int matrixRow = 0; matrixRow <= matrixBorder[x]; matrixRow++) {
					for (int matrixCol = 0; matrixCol <= matrixBorder[y]; matrixCol++) {
						// ok, now we are in the loop for convolution matrix, that's centered on the [row][col]
						// let's calculate the the coordinates in the image
						int offsetX = (matrixCenter[x] - matrixRow);
						int offsetY = (matrixCenter[y] - matrixCol);
						int imageX = row - offsetX;
						int imageY = col - offsetY;
							
						newValue += (inputImage.get(imageX, imageY)[0] * (int)convolutionMatrix[matrixRow][matrixCol]);
						
					}
				}
				
				resultImage.put(row, col, newValue);
			}
		}
		
		onWorkingLoopEnd();
	}

}
