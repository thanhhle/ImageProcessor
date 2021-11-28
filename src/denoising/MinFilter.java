package denoising;

import java.awt.image.BufferedImage;
import java.util.List;

import processing.Processor;

public class MinFilter implements DenoisingFilter
{
	/**
	 * Class constructor
	 */
	public MinFilter()
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
				
				// Initialize array to store RGB value of the pixel at the center of the mask
				int rgb[] = new int[] {255, 255, 255};
				
				for(int i = 0; i < rgbValues.size(); i++)
				{
					for(int k = 0; k < 3; k++)
					{
						rgb[k] = rgbValues.get(i)[k] < rgb[k] ? rgbValues.get(i)[k] : rgb[k];
					}
				}
				
				// Set the value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
}
