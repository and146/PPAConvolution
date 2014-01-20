/**
 * 
 */
package and146.projects.convolution.opencv;

import org.opencv.highgui.Highgui;

/**
 * @author neonards
 *
 */
public class InMemoryGrayscaleImage extends InMemoryImage {

	public InMemoryGrayscaleImage(String filePath) {
		super(filePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
	}

}
