package processing;

import java.awt.image.BufferedImage;

public class PixelSizeConverter implements Processor
{
	/**
	 * Class constructor
	 */
	public PixelSizeConverter()
	{
		
	}
	
	
	/**
	 * Convert image to n-bit color image
	 * Image is converted to grayscale if it is not yet in monochrome
	 * @param image the original image
	 * @param pixelSize the number of bits each pixel contains
	 * @return the processed image
	 */
	public BufferedImage process(BufferedImage image, int pixelSize)
	{
		// Return the original image if the pixelSize is unchanged
		if(pixelSize == image.getColorModel().getPixelSize())
		{
			return image;
		}
		
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		// Convert image to grayscale if it's not yet in grayscale
		if(image.getType() != BufferedImage.TYPE_BYTE_GRAY)
		{
			image = Processor.convertToGrayscale(image);
		}
		
		// Get the number of colors made from pixel size of the origin image
		int imageNumberOfColors = (int)Math.pow(2, image.getColorModel().getPixelSize());
		
		// Get the number of colors made from the new input pixel size
		int newNumberOfColors = (int)Math.pow(2, pixelSize);
		
		// Calculate the ratio the pixel value to be scaled to
		double ratio = (imageNumberOfColors - 1) / (newNumberOfColors - 1);
	
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				// Get the pixel value of at (x, y)
				int pixel = image.getRaster().getSample(x, y, 0);
				
				// Convert the pixel value to the associated n-bit color value
				pixel = (int) (Math.round(pixel / ratio) * ratio);
				
				// Set the new value to the pixel(x, y)
				processedImage.getRaster().setSample(x, y, 0, pixel);
			}
		}
	
		return processedImage;	
	}
}
