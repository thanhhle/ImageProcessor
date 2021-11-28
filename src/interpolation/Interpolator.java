package interpolation;

import java.awt.image.BufferedImage;

import processing.Processor;


public interface Interpolator extends Processor
{
	enum Direction {X, Y}
	
	
	/**
	 * Transform the spatial resolution of an image to the given width and height
	 * This method works well with colored image
	 * @param image the original image
	 * @param width the resized width
	 * @param height the resized height
	 * @return the resized image with given width and height
	 */
	BufferedImage process(BufferedImage image, int width, int height);
}
