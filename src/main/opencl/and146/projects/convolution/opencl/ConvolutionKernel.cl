__kernel void perform_convolution(__global const float* inImg, 
									__global const float* convMat, 
									__global float* out, 
									int inImgRows, int inImgCols, 
									int convMatRows, int convMatCols) 
{
    // current index in input image vector
    int i = get_global_id(0);
	
	// calculate from index the current row and column in the input image
	int row = i / inImgCols;
    int col = (i < inImgCols ? i : (i - ((row+1)*inImgCols)));

	
	// matrix indexes for coords (x=0/y=1)
	const int x = 0;
	const int y = 1;
	
	// calculate convolution matrix's center indexes
	int matrixCenter[2];
	matrixCenter[x] = convMatRows / 2; // x
	matrixCenter[y] = convMatCols / 2; // y
				
	// calculate convolution matrix's border indexes
	int matrixBorder[2];
	matrixBorder[x] = convMatRows - 1; // x
	matrixBorder[y] = convMatCols - 1; // y
				
	// process the input image and save the result to the result image
	int beginRowIndex = matrixCenter[x];
	int endRowIndex = inImgRows - 1 - matrixCenter[x];
	int beginColIndex = matrixCenter[y];
	int endColIndex = inImgCols - 1 - matrixCenter[y];
	
	float newValue = 0.0;
	
	// let's apply
	for (int matrixRow = 0; matrixRow <= matrixBorder[x]; matrixRow++) {
		for (int matrixCol = 0; matrixCol <= matrixBorder[y]; matrixCol++) {
			// ok, now we are in the loop for convolution matrix, that's centered on the [row][col]
			// let's calculate the the coordinates in the image
			int offsetX = (matrixCenter[x] - matrixRow);
			int offsetY = (matrixCenter[y] - matrixCol);
			int imageX = row - offsetX;
			int imageY = col - offsetY;
			
			int imageVectorIndex = imageX * inImgCols + imageY;
			int matrixVectorIndex = matrixRow * convMatCols + matrixCol;
			
			newValue += (inImg[imageVectorIndex] * convMat[matrixVectorIndex]);
			
		}
	}
	
	out[i] = newValue;
	
}