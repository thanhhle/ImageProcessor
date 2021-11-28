package denoising;

import java.awt.image.BufferedImage;

import processing.Processor;

public interface DenoisingFilter extends Processor
{
	/**
	 * Remove noise from a given image
	 * @param image the original image
	 * @param kernelWidth the width of the kernel
	 * @param kernelHeight the height of the kernel
	 * @return the denoised image
	 */
	public BufferedImage process(BufferedImage image, int subimageWidth, int subimageHeight);
}
