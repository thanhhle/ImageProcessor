package interpolation;

import java.awt.image.BufferedImage;

import processing.Processor;

public class LinearInterpolator implements Interpolator
{
	private Interpolator.Direction direction;
	
	
	/**
	 * Class constructor
	 * @param direction the direction to be interpolated
	 */
	public LinearInterpolator(Interpolator.Direction direction)
	{
		this.direction = direction;
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
		double widthRatio = image.getWidth() < width ? ((double)(image.getWidth() - 1)/width) : (double)image.getWidth()/width; 
		double heightRatio = image.getHeight() < height ? ((double)(image.getHeight() - 1)/height) : (double)image.getHeight()/height;
		
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{		
				// Find the 2 closest coordinates in each direction (x and y)
				int x0 = (int)Math.floor(x * widthRatio);		
				int y0 = (int)Math.floor(y * heightRatio);
				int x1 = x0 + 1;
				int y1 = y0 + 1;

				// Initialize array to store RGB values of the 2 pixels to be interpolated
				int[] rgb0 = new int[3];
				int[] rgb1 = new int[3];
				
				double ratio = 0.0;
				int pos = 0;
				int pos0 = 0;
				int pos1 = 0;
						
				// Select the variable to be used when applying linear interpolation in x or in y direction
				if(this.direction == Interpolator.Direction.X)
				{
					ratio = widthRatio;
					pos = x;
					pos0 = x0;
					pos1 = x1;
					rgb0 = Processor.getRGB(image, pos0, y0);
					rgb1 = Processor.getRGB(image, pos1, y0);
				}
				else
				{
					ratio = heightRatio;
					pos = y;
					pos0 = y0;
					pos1 = y1;
					rgb0 = Processor.getRGB(image, x0, pos0);
					rgb1 = Processor.getRGB(image, x0, pos1);
				}
				
				// Calculate the RGB value for the pixel(x, y) by linear interpolation
				int[] rgb = linearInterpolate(rgb0, rgb1, ratio, pos, pos0, pos1);
			
				// Set the RGB value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
	

	// Perform linear interpolation on 3 color channels (red, blue, and green) of the pixel(x, y) with given data of 2 other pixel values
	private int[] linearInterpolate(int pixel0[], int pixel1[], double ratio, int pos, int pos0, int pos1)
	{
		int[] rgb = new int[3];
		for(int i = 0; i < 3; i++)
		{
			rgb[i] = linearInterpolate(pixel0[i], pixel1[0], ratio, pos, pos0, pos1);
		}

		return rgb;
	}
	
	
	// Calculate linear interpolation value of the pixel(x, y) with given data of 2 other pixel values
	private int linearInterpolate(int pixel0, int pixel1, double ratio, int pos, int pos0, int pos1)
	{
		return (int) (pixel0 + (pos * ratio - pos0) * (pixel1 - pixel0)/(pos1 - pos0));
	}
	
}