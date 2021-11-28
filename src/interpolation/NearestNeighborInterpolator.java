package interpolation;
import java.awt.image.BufferedImage;

import processing.Processor;

public class NearestNeighborInterpolator implements Interpolator
{
	/**
	 * Class constructor
	 */
	public NearestNeighborInterpolator()
	{
		
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int width, int height)
	{
		// Return the original image if the size is unchanged
		if(width == image.getWidth() && height == image.getHeight())
		{
			return image;
		}
		
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(width, height, imageType);
				
		// Calculate the ratio between the original image's size and processed image'size
		double widthRatio = (double)image.getWidth()/width;
		double heightRatio = (double)image.getHeight()/height;
		
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				// Find the coordinate of the nearest neighbor
				int nearestX = (int)Math.floor(x * widthRatio);
				int nearestY = (int)Math.floor(y * heightRatio);				
				
				// Get the RGB value of the nearest neighbor
				int rgb[] = Processor.getRGB(image, nearestX, nearestY);
				
				// Set the RGB value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
}
