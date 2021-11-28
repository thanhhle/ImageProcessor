package imnoising;

import java.awt.image.BufferedImage;
import java.util.Random;

import processing.Processor;

public class PoissonNoiseFilter implements ImnoisingFilter
{
	/**
	 * Class constructor
	 */
	public PoissonNoiseFilter()
	{
		
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
				
				// Calculate the noise and replace it with each RGB value
				for(int i = 0; i < 3; i++)
				{
					rgb[i] = generatePoissonNoise(rgb[i]);
				}
				
				// Set the clipped RGB value to the pixel (x, y)
				Processor.setRGB(processedImage, x, y, Processor.clipRGB(rgb));
			}
		}
		
		return processedImage;
	}
	
	
	/**
	 * Generate a noise that is modeled by a Poisson distribution
	 * @param pixel the pixel value
	 * @return the generated Poisson noise
	 */
	private int generatePoissonNoise(int pixel)
	{
		double L = Math.exp(-(pixel));
		int k = 0;
		double p = 1;
		
		do 
		{
			k++;
			p *= new Random().nextDouble();
		} 
		while (p >= L);
		
		return k - 1;
	}
}
