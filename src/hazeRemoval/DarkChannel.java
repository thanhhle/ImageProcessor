package hazeRemoval;

import java.awt.image.BufferedImage;

import processing.Processor;

public class DarkChannel implements HazeRemover
{
	/**
	 * Class constructor
	 */
	public DarkChannel()
	{
		
	}

	@Override
	public BufferedImage process(BufferedImage image, int patchRadius) 
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
						
		// Get the dark channel values
		double[][] darkChannel = HazeRemover.getDarkChannel(HazeRemover.getRGBs(image), patchRadius);
		
		// Set the dark channel values to the corresponding coordinate of the created image
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Use the same dark channel values for all channels as dark channel is in grayscale
				int[] rgb = new int[] {(int) darkChannel[x][y], (int) darkChannel[x][y], (int) darkChannel[x][y]};
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
				
		return processedImage;
	}
}
