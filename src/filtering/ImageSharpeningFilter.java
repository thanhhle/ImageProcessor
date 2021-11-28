package filtering;

import java.awt.image.BufferedImage;

import processing.Processor;

public class ImageSharpeningFilter implements SpatialFilter
{
	private String mode;
	
	
	/**
	 * Class constructor
	 */
	public ImageSharpeningFilter(String mode)
	{
		this.mode = mode;
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		// Generate the Laplacian kernel
		double[][] kernel = new double[][] {{1, 1, 1}, {1, -8, 1}, {1, 1, 1}};	

		// Initialize the array to store RGB values of the image
		int[][][] rgbValues = new int[image.getWidth()][image.getHeight()][3];
		
		// Initialize the array to store max and min RGB values of the image
		int[] max = {255, 255, 255};
		int[] min = {0, 0, 0};
		
		// Determine the value of c
		double c = kernel[1][1] < 0 ? -1 : 1;
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Get the RGB value of the centered pixel(x, y) from the original image
				int rgb[] = Processor.getRGB(image, x, y);
				
				// Get the Laplacian value of the centered pixel(x, y) by performing convolution on the pixels under the mask
				int laplacianRGB[] = SpatialFilter.convolve(image, x, y, kernel);
				
				for(int i = 0; i < 3; i++)
				{
					// Calculate the new RGB value
					rgb[i] = (int) (rgb[i] + c * laplacianRGB[i]);
					
					// Record max and min values for scaling
					min[i] = rgb[i] < min[i] ? rgb[i] : min[i];
					max[i] = rgb[i] > max[i] ? rgb[i] : max[i];
				}
				
				// Record the RGB value of pixel(x, y)
				rgbValues[x][y] = rgb;
			}	
		}
			
		
		// Process the image on chosen mode: clipping or scaling
		if(this.mode.equals("clipping"))
		{
			SpatialFilter.clipPixels(processedImage, rgbValues);	
		}
		else
		{
			SpatialFilter.scalePixels(processedImage, rgbValues, max, min);
		}
				
		return processedImage;
	}
}
