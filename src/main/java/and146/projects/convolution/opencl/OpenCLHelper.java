/**
 * 
 */
package and146.projects.convolution.opencl;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for OpenCL library.
 * 
 * @author neonards
 *
 */
public class OpenCLHelper {
	
	private static Logger log = LoggerFactory.getLogger(OpenCLHelper.class);
	
	/**
	 * Returns the URL of required OpenCL source code.
	 * 
	 * @param packageName Package that contains the OpenCL source code
	 * @param sourceFile Name of OpenCL source code file
	 * @return
	 */
	public static URL getOpenClSourceCodeUrl(String packageName, String sourceFile) {
		if (packageName == null || packageName.trim().isEmpty()
				|| sourceFile == null || sourceFile.trim().isEmpty())
			throw new IllegalArgumentException("Paths cannot be null.");
		
		packageName = packageName.trim();
		sourceFile = sourceFile.trim();
		
		if (!sourceFile.endsWith(".cl"))
			throw new IllegalArgumentException(String.format("Expected '.cl' extension, but found '%s'.", sourceFile.substring(sourceFile.lastIndexOf("."))));
		
		String resourcePath = "/" + packageName.replaceAll("\\.", "/") + "/" + sourceFile;
		
		log.debug(String.format("Resolving OpenCL source code '%s' to filepath '%s'.", packageName + "." + sourceFile, resourcePath));
		
		return OpenCLHelper.class.getResource(resourcePath);
	}
	
}
