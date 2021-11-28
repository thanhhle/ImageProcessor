package processing;

import java.awt.image.BufferedImage;

public class HistogramEqualizator implements Processor
{
	/**
	 * Class constructor
	 */
	public HistogramEqualizator()
	{
		
	}
	
	
	/**
	 * Perform local histogram equalization on the image with given kernel radius
	 * @param image the original image
	 * @param kernelRadius the radius of the kernel
	 * @return the processed image
	 */
	public BufferedImage process(BufferedImage image, int kernelRadius)
	{
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		int L = 256;
		int M = kernelRadius * 2 + 1; 
		int N = kernelRadius * 2 + 1;
		
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				// Initialize array to store the frequency of each pixel (0-255) under the mask
				int intensityFreqs[][] = new int[3][L];
				
				// Loop over all pixels under the mask
				for(int i = -kernelRadius; i <= kernelRadius; i++)
				{
					for(int j = -kernelRadius; j <= kernelRadius; j++)
					{
						int w = x + i;
						int h = y + j;
						
						// Ignore the pixel that is outside the image's edges
						if(w >= 0 && w < image.getWidth() && h >= 0 && h < image.getHeight())
						{
							// Get the RGB value of the pixel(x, y)
							int[] rgb = Processor.getRGB(image, w, h);
							
							// Add up its frequency
							intensityFreqs[0][rgb[0]]++;
							intensityFreqs[1][rgb[1]]++;
							intensityFreqs[2][rgb[2]]++;
						}
					}
				}
				
				// Get the histogram equalization
				int[][] histogram = getHistogram(image, L, M, N, intensityFreqs);
				
				// Get the RGB value of the centered pixel(x, y)
				int[] rgb = Processor.getRGB(image, x, y);
				
				// Apply a transformation to all RGB channels by the histogram
				rgb[0] = histogram[0][rgb[0]];
				rgb[1] = histogram[1][rgb[1]];
				rgb[2] = histogram[2][rgb[2]];
				
				// Set the RGB value to the centered pixel at (x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
	
	
	/**
	 * Perform global histogram equalization on the input image
	 * @param image the original image
	 * @return the processed image
	 */
	public BufferedImage process(BufferedImage image)
	{		
		// Create a new image with the same image type as the original image
		int imageType = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
		
		int L = 256;
		int M = image.getWidth(); 
		int N = image.getHeight();
		
		// Initialize array to store the frequency of each pixel (0-255)
		int intensityFreqs[][] = new int[3][L];

		for(int x = 0; x < M; x++)
		{
			for(int y = 0; y < N; y++)
			{		
				// Get the RGB value of the pixel(x, y)
				int[] rgb = Processor.getRGB(image, x, y);
				
				// Add up its frequency
				intensityFreqs[0][rgb[0]]++;
				intensityFreqs[1][rgb[1]]++;
				intensityFreqs[2][rgb[2]]++;
			}
		}
		
		// Get the histogram equalization
		int[][] histogram = getHistogram(image, L, M, N, intensityFreqs);

		for(int x = 0; x < M; x++)
		{
			for(int y = 0; y < N; y++)
			{
				// Get the RGB value of the centered pixel(x, y)
				int[] rgb = Processor.getRGB(image, x, y);
				
				// Apply a transformation to all RGB channels by the histogram
				rgb[0] = histogram[0][rgb[0]];
				rgb[1] = histogram[1][rgb[1]];
				rgb[2] = histogram[2][rgb[2]];
				
				// Set the RGB value to the centered pixel at (x, y)
				Processor.setRGB(processedImage, x, y, rgb);
			}
		}
		
		return processedImage;
	}
	
	
	/**
	 * Get the equalized histogram of the input image
	 * @param image the original image
	 * @param L the number of intensity levels
	 * @param M the width of the area to be performed histogram equalization
	 * @param N the height of the area to be performed histogram equalization
	 * @param intensityFreqs the frequency of intensity levels
	 * @return the equalized histogram
	 */
	private int[][] getHistogram(BufferedImage image, int L, int M, int N, int[][] intensityFreqs)
	{
		int[][] histogram = new int[3][L];
		
		// Initialize the array to store the cumulative frequency of 3 RGB channels
		int cumulativeFreq[] = {0, 0, 0};
		
		for (int i = 0; i < histogram.length; i++)
        {
			for(int j = 0; j < histogram[0].length; j++)
			{
				// Add up the cumulative frequency
				cumulativeFreq[i] += intensityFreqs[i][j];
				
				// Calculate the new mapping for each intensity
				histogram[i][j] = (int) ((L - 1) * cumulativeFreq[i]/(M * N));
			}
        }
		
		return histogram;		
	}
}
