package filtering;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import processing.Processor;

public class AveragingFilter implements SpatialFilter
{
	/**
	 * Class constructor
	 */
	public AveragingFilter()
	{
		
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		// Generate the averaging kernel with given radius
		double[][] kernel = generateAveragingKernel(kernelRadius);
		
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
	 * Generate an averaging kernel with given size
	 * @param kernelWidth the width of the kernel
	 * @param kernelHeight the height of the kernel
	 * @return the averaging kernel
	 */
	private double[][] generateAveragingKernel(int kernelRadius)
	{
		double[][] kernel = new double[kernelRadius * 2 + 1][kernelRadius * 2 + 1];
		
		int kernelSize = (kernelRadius * 2 + 1) * (kernelRadius * 2 + 1);
		
		// Fill all the values in the kernel with 1/(sum of coefficients)
		Arrays.stream(kernel).forEach(i -> Arrays.fill(i, 1.0/kernelSize));
		
		return kernel;
	}
}
