package imnoising;

import java.awt.image.BufferedImage;
import java.util.Random;

import processing.Processor;

public class SaltAndPepperNoiseFilter implements ImnoisingFilter
{
	private String type;
	private double density;
	
	/**
	 * Class constructor
	 */
	public SaltAndPepperNoiseFilter(String type, double density)
	{
		this.type = type;
		this.density = density;
	}
	
	
	public BufferedImage process(BufferedImage image)
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				int[] rgb = new int[3];
				
				// Generate a random probability value from a standard uniform distribution on the interval (0, 1)
				double randProb = 0.0;
				while(randProb == 0.0)
				{
					randProb = new Random().nextDouble();
				}
				
				// Determine the new RGB based on the random probability
				if(this.type.equals("salt"))
				{
					rgb = generateSaltNoise(randProb);
				}
				
				else if(this.type.equals("pepper"))
				{
					rgb = generatePepperNoise(randProb);
				}
				
				else
				{
					rgb = generateSaltAndPepperNoise(randProb);
				}
				
				rgb = rgb == null ? Processor.getRGB(image, x, y) : rgb;
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
	

	private int[] generateSaltAndPepperNoise(double randProb)
	{
		// For pixels with probability value in the range (0, d/2), the pixel value is set to 0
		if(randProb < this.density/2)
		{
			return new int[] {0, 0, 0};
		}
		
		// For pixels with probability value in the range [d/2, d), the pixel value is set to 255
		else if((randProb >= this.density/2) && (randProb < this.density))
		{
			return new int[] {255, 255, 255};
		}
		
		// For pixels with probability value in the range [d, 1), the pixel value is unchanged
		else
		{
			 return null;
		}
	}
	
	
	private int[] generateSaltNoise(double randProb)
	{
		// For pixels with probability value in the range (0, d/2], the pixel value is set to 255
		if(randProb <= this.density/2)
		{
			return new int[] {255, 255, 255};
		}
		
		// For pixels with probability value in the range (d/2, 1), the pixel value is unchanged
		else
		{
			 return null;
		}
	}
	
	
	private int[] generatePepperNoise(double randProb)
	{
		// For pixels with probability value in the range (0, d/2], the pixel value is set to 0
		if(randProb <= this.density/2)
		{
			return new int[] {0, 0, 0};
		}
		
		// For pixels with probability value in the range (d/2, 1), the pixel value is unchanged
		else
		{
			 return null;
		}
	}
}
