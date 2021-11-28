package denoising;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.Processor;

public class AlphaTrimmedMeanFilter implements DenoisingFilter
{
	private int d;
	
	/**
	 * Class constructor
	 */
	public AlphaTrimmedMeanFilter(int d)
	{
		this.d = d;
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
						
				// Store each color intensity in an associated list
				List<Integer> red = new ArrayList<Integer>();
				List<Integer> green = new ArrayList<Integer>();
				List<Integer> blue = new ArrayList<Integer>();			
				for(int i = 0; i < rgbValues.size(); i++)
				{	
					red.add(rgbValues.get(i)[0]);
					green.add(rgbValues.get(i)[1]);
					blue.add(rgbValues.get(i)[2]);
				}
				
				// Sort the lists
				Collections.sort(red);
				Collections.sort(green);
				Collections.sort(blue);
				
				// Initialize array to store RGB value of the pixel at the center of the mask
				int rgb[] = new int[3];
				for(int i = this.d/2; i < rgbValues.size() - this.d/2; i++)
				{
					rgb[0] += (red.get(i) / (rgbValues.size() - d));
					rgb[1] += (green.get(i) / (rgbValues.size() - d));
					rgb[2] += (blue.get(i) / (rgbValues.size() - d));
				}

				// Set the value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
}
