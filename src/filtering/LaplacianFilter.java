package filtering;

import java.awt.image.BufferedImage;

public class LaplacianFilter implements SpatialFilter
{
	private String mode;
	
	/**
	 * Class constructor
	 */
	public LaplacianFilter(String mode)
	{
		this.mode = mode;
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		// Generate the Laplacian kernel
		double[][] kernel = new double[][] {{1, 1, 1}, {1, -8, 1}, {1, 1, 1}};	
		
		// Initialize the array to store RGB values of the image
		int[][][] rgbValues = new int[image.getWidth()][image.getHeight()][3];
		
		// Initialize the array to store max and min RGB values of the image
		int[] max = {255, 255, 255};
		int[] min = {0, 0, 0};
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Calculate the RGB value of the centered pixel(x, y) by performing convolution on the pixels under the mask
				int rgb[] = SpatialFilter.convolve(image, x, y, kernel);
				
				// Record max and min RGB values
				for(int i = 0; i < rgb.length; i++)
				{	
					min[i] = rgb[i] < min[i] ? rgb[i] : min[i];
					max[i] = rgb[i] > max[i] ? rgb[i] : max[i];
				}
				
				// Record the RGB value of pixel(x, y)
				rgbValues[x][y] = rgb;
			}	
		}
		
		// Process the image on chosen mode: clipping or scaling
		if(this.mode.equals("clipping"))
		{
			SpatialFilter.clipPixels(processedImage, rgbValues);	
		}
		else
		{
			SpatialFilter.scalePixels(processedImage, rgbValues, max, min);
		}
		
		return processedImage;
	}
	
	
	/**
	 * Generate a Laplacian kernel with given radius
	 * @param kernelRadius the radius of the kernel
	 * @return the Laplacian kernel
	 */
	/*
	private double[][] generateLaplacianKernel(int kernelRadius)
	{
		double[][] kernel = new double[kernelRadius * 2 + 1][kernelRadius * 2 + 1];
		
		// Fill all the values in the kernel with 1/centerValue
		Arrays.stream(kernel).forEach(i -> Arrays.fill(i, -1));
		
		// Set the value of the center pixel
		kernel[kernelRadius][kernelRadius] = (Math.pow(kernelRadius * 2 + 1, 2) - 1);
		
		return kernel;
	}
	*/
	
	
	/**
	 * Generate a Laplacian kernel with given radius
	 * @param kernelRadius the radius of the kernel
	 * @return the Laplacian kernel
	 */
	/*
	private double[][] generateLapacianOfGuassianKernel(int kernelRadius)
	{
		double[][] kernel = new double[kernelRadius * 2 + 1][kernelRadius * 2 + 1];
		
		// Calculate the value for each position in the kernel by Gaussian function
		double sigma = 1.4;
		for(int x = 0; x < kernel.length; x++) 
		{
			for(int y = 0; y < kernel[0].length; y++) 
			{
				double d = Math.sqrt(Math.pow(x - kernelRadius, 2) + Math.pow(y - kernelRadius, 2));
				
				kernel[x][y] = (-1/(Math.PI * Math.pow(sigma, 4))) *
							   (1 - (Math.pow(d, 2)/(2 * Math.pow(sigma, 2)))) *
							   (Math.exp(-Math.pow(d, 2)/(2 * Math.pow(sigma, 2))));
		    }
		}
		
		return kernel;
	}
	*/
}