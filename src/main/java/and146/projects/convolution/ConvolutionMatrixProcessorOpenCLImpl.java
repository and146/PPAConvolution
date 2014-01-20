/**
 * 
 */
package and146.projects.convolution;

import static org.bridj.Pointer.allocateFloats;

import java.io.IOException;
import java.nio.ByteOrder;

import org.bridj.Pointer;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import and146.projects.convolution.opencl.OpenCLHelper;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;

/**
 * Parallel (OpenCL) implementation of IConvolutionMatrixProcessor
 * 
 * @author neonards
 *
 */
public class ConvolutionMatrixProcessorOpenCLImpl extends AbstractConvolutionMatrixProcessor {
	
	private Logger log = LoggerFactory.getLogger(ConvolutionMatrixProcessorOpenCLImpl.class);
	//
	private static final String OPENCL_KERNEL_SOURCE_PACKAGE = "and146.projects.convolution.opencl";
	private static final String OPENCL_KERNEL_SOURCE_FILE = "ConvolutionKernel.cl";
	private static final String OPENCL_KERNEL_NAME = "perform_convolution";
	//
	private CLContext context;
	private CLProgram program;
	
	public ConvolutionMatrixProcessorOpenCLImpl() {
		this(DEFAULT_MEASUREMENT_TYPE);
	}
	
	public ConvolutionMatrixProcessorOpenCLImpl(MeasurementType measurementType) {
		super(measurementType);
		
		// to pre-load the OpenCL stuff (messes up the time measuring)
		log.debug("Getting OpenCL context...");
		context = getCLContext();
		
		// Read the program sources and compile them :
		log.debug("Reading the OpenCL program sources and compile them...");
        String src = null;
		try {
			src = IOUtils.readText(OpenCLHelper.getOpenClSourceCodeUrl(OPENCL_KERNEL_SOURCE_PACKAGE, OPENCL_KERNEL_SOURCE_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
        program = context.createProgram(src);
	}
	
	@Override
	protected void processImage(Mat inputImage, Mat resultImage,
			ConvolutionMatrix matrix) {
		
		CLQueue queue = context.createDefaultQueue();
        ByteOrder byteOrder = context.getByteOrder();
        
        int pixelsCount = inputImage.rows() * inputImage.cols();
        int convMatItemsCount = matrix.getMatrix().length * matrix.getMatrix()[0].length;
        
        Pointer<Float> inPointer = allocateFloats(pixelsCount).order(byteOrder);
        Pointer<Float> convPointer = allocateFloats(convMatItemsCount).order(byteOrder);
        
        // fill the input image vector
        int indexCounter = 0;
        for (int r = 0; r < inputImage.rows(); r++) {
			for (int c = 0; c < inputImage.cols(); c++) {
				inPointer.set(indexCounter, (float) inputImage.get(r, c)[0]);
				indexCounter++;
			}
		}
		
        // fill the convolution matrix vector
        indexCounter = 0;
        for (int r = 0; r < matrix.getMatrix().length; r++) {
			for (int c = 0; c < matrix.getMatrix()[0].length; c++) {
				convPointer.set(indexCounter, (float) matrix.getMatrix()[r][c]);
				indexCounter++;
			}
		}
        
        // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
        CLBuffer<Float> 
            inBuffer = context.createBuffer(Usage.Input, inPointer),
            convMatBuffer = context.createBuffer(Usage.Input, convPointer);
        
        // Create an OpenCL output buffer :
        CLBuffer<Float> outBuffer = context.createBuffer(Usage.Output, Float.class, pixelsCount); // kurvitko
        
        // Get and call the kernel :
        CLKernel addFloatsKernel = program.createKernel(OPENCL_KERNEL_NAME);
        addFloatsKernel.setArgs(inBuffer, 
        		convMatBuffer, 
        		outBuffer, 
        		inputImage.rows(), inputImage.cols(), 
        		matrix.getMatrix().length, matrix.getMatrix()[0].length);
        
        onWorkingLoopStart();
        CLEvent addEvt = addFloatsKernel.enqueueNDRange(queue, new int[] { pixelsCount });
        
        Pointer<Float> outPtr = outBuffer.read(queue, addEvt); // blocks until add_floats finished
        onWorkingLoopEnd();
        
        // convert back to matrix
        for (int i = 0; i < pixelsCount; i++) {
            int row = i / inputImage.cols();
            int col = (i < inputImage.cols() ? i : (i - ((row+1)*inputImage.cols())));
            
            resultImage.put(row, col, outPtr.get(i));
        }
        
	}
	
	private CLContext getCLContext() {
		CLContext context = null;
		try {
			context = JavaCL.createBestContext();
		} catch(Exception e) {
			log.error("It seems that this computer doesn't support OpenCL.");
			throw e;
		}
		return context;
	}
}
