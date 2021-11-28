package hazeRemoval;

import java.awt.image.BufferedImage;

import processing.Processor;

public class TransmissionMap implements HazeRemover 
{
	private boolean withErosion;
	private boolean withRefinement;
	
	/**
	 * Class constructor
	 */
	public TransmissionMap(boolean withErosion, boolean withRefinement)
	{
		this.withErosion = withErosion;
		this.withRefinement = withRefinement;
	}
	
	
	@Override
	public BufferedImage process(BufferedImage image, int patchRadius) 
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		// Get the transmission map
		double[][][] transmissionMap = HazeRemover.getTranmissionMap(HazeRemover.getRGBs(image), patchRadius, this.withErosion, this.withRefinement);
		
		// Set the transmission values to the corresponding coordinate of the created image
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// For visualization, transmission values are multiplied by 255
				int[] rgb = new int[] {	(int) (transmissionMap[x][y][0]), 
										(int) (transmissionMap[x][y][1]),
										(int) (transmissionMap[x][y][2]) };
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
				
		return processedImage;
	}
}
