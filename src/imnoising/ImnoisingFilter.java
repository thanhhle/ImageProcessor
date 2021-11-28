package imnoising;

import java.awt.image.BufferedImage;

import processing.Processor;

public interface ImnoisingFilter extends Processor
{
	/**
	 * Imnoise a given image
	 * @param image the original image
	 * @return the noised image
	 */
	public BufferedImage process(BufferedImage image);
}
