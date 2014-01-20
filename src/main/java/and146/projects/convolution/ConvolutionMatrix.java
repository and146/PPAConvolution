/**
 * 
 */
package and146.projects.convolution;

/**
 * @author neonards
 * 
 * Wrapper for convolution matrix
 * 
 */
public class ConvolutionMatrix {
	private String name;
	private int[][] matrix;
	
	public ConvolutionMatrix(String name, int[][] matrix) {
		this.name = name;
		this.matrix = matrix;
		
		validate();
	}
	
	private void validate() {
		if (name == null || name.trim().length() == 0)
			throw new IllegalStateException("Invalid name for convolution matrix.");
		
		if (matrix == null)
			throw new IllegalArgumentException("The matrix must be set.");
		
		if (matrix.length == 0 || matrix[0].length == 0)
			throw new IllegalArgumentException("The matrix must have MxN dimension, where M and N are >0.");
		
		if (matrix.length % 2 != 1 || matrix[0].length %2 != 1)
			throw new IllegalArgumentException("The matrix must have odd-sized rows and cols.");
	}
	
	public String getName() {
		return name;
	}
	
	public int[][] getMatrix() {
		return matrix;
	}
}
