package filtering;

import java.awt.image.BufferedImage;

import processing.Processor;

public class HighBoostingFilter implements SpatialFilter
{
	private double k;
	
	/**
	 * Class constructor
	 */
	public HighBoostingFilter(double k)
	{
		this.k = k;
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{
		// Blur the image
		BufferedImage processedImage = new MedianFilter().process(image, kernelRadius);
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Get the RGB value of the centered pixel(x, y) from the original image 
				int[] rgb = Processor.getRGB(image, x, y);
				
				// Get the RGB value of the centered pixel(x, y) from the blurred image 
				int[] blurRGB = Processor.getRGB(processedImage, x, y);
				
				// Calculate new value for the pixel by smoothing function
				for(int i = 0; i < 3; i++)
				{
					// Calculate the unsharp mask
					double mask = rgb[i] - blurRGB[i];
					
					// Calculate the new RGB value by adding a weighted portion k of the mask back to the original one
					rgb[i] = (int) (rgb[i] + k * mask);
				}
				
				// Set the clipped RGB value to the pixel (x, y)
				Processor.setRGB(processedImage, x, y, Processor.clipRGB(rgb));
			}
		}
		
		return processedImage;
	}
}