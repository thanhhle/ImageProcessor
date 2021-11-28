package filtering;

import java.awt.image.BufferedImage;

import processing.Processor;

public class GaussianFilter implements SpatialFilter
{
	private double sigma;
	
	
	/**
	 * Class constructor
	 */
	public GaussianFilter(double sigma)
	{
		this.sigma = sigma;
	}
	

	@Override
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{				
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		// Generate the Gaussian kernel with given radius
		double[][] kernel = generateGaussianKernel(kernelRadius);
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Calculate the RGB value of the centered pixel(x, y) by performing convolution on the pixels under the mask
				int[] rgb = SpatialFilter.convolve(image, x, y, kernel);
						
				// Set the value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
	
	
	/**
	 * Generate a Gaussian kernel with given radius
	 * @param kernelWidth the width of the kernel
	 * @param kernelHeight the height of the kernel
	 * @return the Gaussian kernel
	 */
	private double[][] generateGaussianKernel(int kernelRadius)
	{
		double[][] kernel = new double[kernelRadius * 2 + 1][kernelRadius * 2 + 1];
		
		// Initialize sum variable for accumulating the kernels values
		double sum = 0.0;
		
		// Calculate the value for each position in the kernel by Gaussian function
		for(int x = 0; x < kernel.length; x++) 
		{
			for(int y = 0; y < kernel[0].length; y++) 
			{
				double d = Math.sqrt(Math.pow(x - kernelRadius, 2) + Math.pow(y - kernelRadius, 2));
				kernel[x][y] = Math.exp(-Math.pow(d, 2)/(2 * Math.pow(this.sigma, 2)));

		        // Accumulate the kernel values
		        sum += kernel[x][y];
		    }
		}

		// Normalize the kernel
		for(int x = 0; x < kernel.length; x++) 
		{
			for(int y = 0; y < kernel[0].length; y++) 
			{
				kernel[x][y] /= sum;
			}
		}
		
		return kernel;
	}
}