package interpolation;

import java.awt.image.BufferedImage;

import processing.Processor;


public class BilinearInterpolator implements Interpolator
{
	/**
	 * Class constructor
	 */
	public BilinearInterpolator()
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
		double widthRatio = image.getWidth() < width ? ((double)(image.getWidth() - 1)/width) : (double)image.getWidth()/width; 
		double heightRatio = image.getHeight() < height ? ((double)(image.getHeight() - 1)/height) : (double)image.getHeight()/height;
				
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				// Find the 2 closest coordinates in each direction (x and y)
				int x1 = (int)Math.floor(x * widthRatio);
				int y1 = (int)Math.floor(y * heightRatio);
				int x2 = x1 + 1;
				int y2 = y1 + 1;

				// Initialize array to store RGB values of 4 nearest neighbors
				int[] Q11 = Processor.getRGB(image, x1, y1);
				int[] Q12 = Processor.getRGB(image, x1, y2);
				int[] Q21 = Processor.getRGB(image, x2, y1);
				int[] Q22 = Processor.getRGB(image, x2, y2);
				
				// Calculate the RGB value for the pixel(x, y) by bilinear interpolation
				int[] rgb = bilinearInterpolate(x * widthRatio, y * heightRatio, x1, x2, y1, y2, Q11, Q12, Q21, Q22);
			
				// Set the RGB value to the pixel(x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}

	
	// Perform bilinear interpolation on 3 color channels (red, blue, and green) of the pixel(x, y) with given data of 4 nearest pixel values
	private int[] bilinearInterpolate(double x, double y, int x1, int x2, int y1, int y2, int[] Q11, int[] Q12, int[] Q21, int[] Q22)
	{
		int[] rgb = new int[3];
		for(int i = 0; i < 3; i++)
		{
			rgb[i] = bilinearInterpolate(x, y, x1, x2, y1, y2, Q11[i], Q12[i], Q21[i], Q22[i]);
		}

		return rgb;
	}
	
	
	// Calculate bilinear interpolation value of the pixel(x, y) with given data of 4 nearest pixel values
	private int bilinearInterpolate(double x, double y, int x1, int x2, int y1, int y2, int Q11, int Q12, int Q21, int Q22)
	{
		double R1 = Q11 * (x2 - x)/(x2 - x1) + Q21 * (x - x1)/(x2 - x1);
		double R2 = Q12 * (x2 - x)/(x2 - x1) + Q22 * (x - x1)/(x2 - x1);
		return (int) (R1 * (y2 - y)/(y2 - y1) + R2 * (y - y1)/(y2 - y1));
	}
}