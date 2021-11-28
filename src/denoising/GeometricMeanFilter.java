package denoising;

import java.awt.image.BufferedImage;
import java.util.List;

import processing.Processor;

public class GeometricMeanFilter implements DenoisingFilter
{
	/**
	 * Class constructor
	 */
	public GeometricMeanFilter()
	{
		
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int subimageWidth, int subimageHeight)
	{	
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Get list of RGB values contained in the neighborhood size m x n of the pixel(x, y)
				List<int[]> rgbValues = Processor.getRGBs(image, x, y, subimageWidth, subimageHeight);
						
				// Initialize array to store product of RGB values in the neighborhood
				double product[] = new double[] {1, 1, 1};
				for(int i = 0; i < rgbValues.size(); i++)
				{
					for(int k = 0; k < 3; k++)
					{
						product[k] *= (rgbValues.get(i)[k]);
					}
				}
				
				// Initialize array to store RGB value of the centered pixel(x, y)
				int rgb[] = new int[3];
				for(int i = 0; i < 3; i++)
				{
					rgb[i] = (int) Math.pow(product[i], 1.0/rgbValues.size());
				}
				
				// Set the value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, Processor.clipRGB(rgb));
			}
		}
		
		return processedImage;
	}
}
