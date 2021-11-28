package filtering;

import java.awt.image.BufferedImage;

import processing.Processor;

public interface SpatialFilter extends Processor
{
	/**
	 * Filter a given image
	 * @param image the original image
	 * @param kernelRadius the radius of the kernel
	 * @return the filtered image
	 */
	public BufferedImage process(BufferedImage image, int kernelRadius);
	
	
	/**
	 * Perform the convolution on the given image
	 * Return the value of centered pixel(x, y)
	 * @param image the original image
	 * @param x x-coordinate of the centered pixel(x, y)
	 * @param y y-coordinate of the centered pixel(x, y)
	 * @param the kernel to be convolved with the image
	 * @return the RGB value of centered pixel(x, y) after convolution
	 */
	public static int[] convolve(BufferedImage image, int x, int y, double[][] kernel)
	{		
		// Initialize array to store RGB value of the pixel at the center of the mask
		int rgb[] = new int[3];
		
		// Loop over all pixels under the mask
		for(int i = -kernel.length/2; i <= kernel.length/2; i++)
		{
			for(int j = -kernel[0].length/2; j <= kernel[0].length/2; j++)
			{
				int w = x + i;
				int h = y + j;
				
				// Ignore the pixel that is outside the image's edges
				if(w >= 0 && w < image.getWidth() && h >= 0 && h < image.getHeight())
				{
					// Get the value of each pixel under the mask
					int[] rgbUnderMask = Processor.getRGB(image, w, h);
					
					// Calculate new value for the pixel
					for(int k = 0; k < rgbUnderMask.length; k++)
					{
						rgb[k] += (rgbUnderMask[k] * kernel[i + kernel.length/2][j + kernel[0].length/2]);
					}
				}
			}
		}
		
		return rgb;
	}
	
	
	/**
	 * Scale image to make its intensities span the full 8-bit scale from 0 to 255
	 * @param image the image whose values to be scaled
	 * @param rgbValues the RGB values to be scaled
	 * @param max the maximum RGB value
	 * @param min the minimum RGB value
	 */
	public static void scalePixels(BufferedImage image, int[][][] rgbValues, int[] max, int[] min)
	{
		for(int x = 0; x < rgbValues.length; x++) 
		{
			for(int y = 0; y < rgbValues[0].length; y++) 
			{
				for(int i = 0; i < 3; i++)
				{
					rgbValues[x][y][i] = (int)((rgbValues[x][y][i] - min[i]) * 255/(max[i] - min[i]));
				}
				
				Processor.setRGB(image, x, y, rgbValues[x][y]);
			}	
		}
	}
	
	
	/**
	 * Clip the image's intensities to make sure it is in the range from 0 to 255
	 * Set all negative values to 0
	 * Set all values that exceed 255 to 255
	 * @param image the image whose values to be clipped
	 * @param rgbValues the RGB values to be clipped
	 */
	public static void clipPixels(BufferedImage image, int[][][] rgbValues)
	{
		for(int x = 0; x < rgbValues.length; x++) 
		{
			for(int y = 0; y < rgbValues[0].length; y++) 
			{
				rgbValues[x][y] = Processor.clipRGB(rgbValues[x][y]);
				Processor.setRGB(image, x, y, rgbValues[x][y]);
			}
		}
	}
}
