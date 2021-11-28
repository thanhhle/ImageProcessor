package imnoising;

import java.awt.image.BufferedImage;
import java.util.Random;

import processing.Processor;

public class GaussianNoiseFilter implements ImnoisingFilter
{
	private int mean;
	private double variance;
	
	/**
	 * Class constructor
	 */
	public GaussianNoiseFilter(int mean, double variance)
	{
		this.mean = mean;
		this.variance = variance;
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image)
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Get the RGB value of the centered pixel(x, y) from the original image 
				int[] rgb = Processor.getRGB(image, x, y);
				
				// Calculate the noise and add to the RGB value of the original image
				int noise = (int) (new Random().nextGaussian() * Math.sqrt(variance) + this.mean);
				for(int i = 0; i < 3; i++)
				{
					rgb[i] += noise;
				}
	
				// Set the clipped RGB value to the pixel (x, y)
				Processor.setRGB(processedImage, x, y, Processor.clipRGB(rgb));
			}
		}
		
		return processedImage;
	}
}
