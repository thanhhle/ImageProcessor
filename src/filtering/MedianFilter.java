package filtering;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.Processor;

public class MedianFilter implements SpatialFilter
{
	/**
	 * Class constructor
	 */
	public MedianFilter()
	{
		
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{			
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
				
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{	
				// Initialize array lists to store RGB values under the mask
				List<Integer> red = new ArrayList<Integer>();
				List<Integer> green = new ArrayList<Integer>();
				List<Integer> blue = new ArrayList<Integer>();
				
				// Loop over all pixels under the mask
				for(int i = -kernelRadius; i <= kernelRadius; i++)
				{
					for(int j = -kernelRadius; j <= kernelRadius; j++)
					{
						int w = x + i;
						int h = y + j;
						
						// Ignore the pixel that is outside the image's edges
						if(w >= 0 && w < image.getWidth() && h >= 0 && h < image.getHeight())
						{			
							// Get the RGB value of pixel at (w, h)
							int rgb[] = Processor.getRGB(image, w, h);
							
							// Add each RGB value to the associated array list
							red.add(rgb[0]);
							green.add(rgb[1]);
							blue.add(rgb[2]);
						}
					}
				}
				
				// Find the median of each list
				int rgb[] = {getMedian(red), getMedian(green), getMedian(blue)};
				
				// Set the RGB value to the centered pixel at (x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
	
	
	/**
	 * Sort the list and find the median
	 * @param list the input list
	 * @return the median of the input list
	 */
	private int getMedian(List<Integer> list)
	{
		Collections.sort(list);
		return list.get((list.size() + 1)/2);
	}
}
