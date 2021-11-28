package processing;

import java.awt.image.BufferedImage;
import java.util.List;

public class BitPlanesRemover implements Processor
{
	/**
	 * Class constructor
	 */
	public BitPlanesRemover()
	{
		
	}
	
	
	/**
	 * Remove bit-planes from an image
	 * @param image the original image
	 * @param removedBitPlanes the bit-planes to be removed
	 * @return the processed image
	 */
	public BufferedImage process(BufferedImage image, List<Integer> removedBitPlanes)
	{	
		// Return the original image if no removed bit-planes is entered
		if(removedBitPlanes.size() == 0)
		{
			return image;
		}
		
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		// Convert image to grayscale if it's not yet in grayscale
		if(image.getType() != BufferedImage.TYPE_BYTE_GRAY)
		{
			image = Processor.convertToGrayscale(image);
		}

		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				// Get the pixel value of at (x, y)
				int pixel = image.getRaster().getSample(x, y, 0);
				
				for(int i = 0; i < removedBitPlanes.size(); i++)
				{
					// Remove the bit associated with the bit planes to be removed
					pixel = pixel & ~(1 << (removedBitPlanes.get(i) - 1));
				}

				// Set the new value to the pixel(x, y)
				processedImage.getRaster().setSample(x, y, 0, pixel);
			}
		}
	
		return processedImage;
	}
}
