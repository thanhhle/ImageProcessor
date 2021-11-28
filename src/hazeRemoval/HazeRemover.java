package hazeRemoval;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.Processor;

public interface HazeRemover extends Processor 
{
	public BufferedImage process(BufferedImage image, int patchRadius);
	
	
	/**
	 * Generate the dark channel of the image
	 * @param rgbs the image's RGB values in array
	 * @param patchRadius the patch radius
	 * @return Dark channel of the image
	 */
	public static double[][] getDarkChannel(double[][][] rgbs, int patchRadius)
	{
		double[][] darkChannel = new double[rgbs.length][rgbs[0].length];
		for(int x = 0; x < darkChannel.length; x++) 
		{
			for(int y = 0; y < darkChannel[0].length; y++) 
			{
				// Get list of RGB values contained in the patch with given radius
				List<double[]> rgbValues = getRGBs(rgbs, x, y, patchRadius);		
				
				// Get the smallest intensity among all color channels of every pixels under the patch
				double min = 255;
				for(double[] rgb : rgbValues)
				{
					min = Math.min(min, Math.min(Math.min(rgb[0], rgb[1]), rgb[2]));
				}
				
				darkChannel[x][y] = min;
			}
		}
		
		return darkChannel;
	}
	
	
	/**
	 * Estimate the atmospheric light
	 * @param rgbs the image's RGB values in array
	 * @param patchRadius the patch radius
	 * @return the atmospheric light
	 */
	public static double[] getAtmosphericLight(double[][][] rgbs, int patchRadius)
	{
		// Generate the dark channel of the image
		double[][] darkChannel = getDarkChannel(rgbs, patchRadius);
		
		// Add the dark channel values to a list
		List<Pixel> darkChannelValues = new ArrayList<Pixel>();
		for(int x = 0; x < darkChannel.length; x++) 
		{
			for(int y = 0; y < darkChannel[0].length; y++) 
			{
				darkChannelValues.add(new Pixel(x, y, darkChannel[x][y]));		
			}
		}	
		
		// Sort the list of dark channel values
		Collections.sort(darkChannelValues);
		
		// Calculate the number of 0.1% brightest pixels in the dark channel
		int numOfBrightestPixels = (int) (darkChannel.length * darkChannel[0].length * 0.1/100);
		
		// Construct a variable to store the highest intensity along the loop
		double highestIntensity = 0;		
		
		// Loop through these brightest pixels and select the one with highest intensity as the atmospheric light
		double[] atmosphericLight = new double[3];
		for(int i = 0; i < numOfBrightestPixels; i++)
		{		
			Pixel pixel = darkChannelValues.get(i);
			
			double[] rgb = rgbs[pixel.posX][pixel.posY];
			double intensity = (rgb[0] + rgb[1] + rgb[2])/3;
			
			if(highestIntensity < intensity)
			{
				highestIntensity = intensity;
				atmosphericLight = rgb;
			}
		}

		return atmosphericLight;
	}
	
	
	/**
	 * Estimate the transmission map
	 * @param rgbs the image's RGB values in array
	 * @param patchRadius the patch radius
	 * @param withErosion if the transmission map be eroded
	 * @return the transmission estimate
	 */
	public static double[][][] getTranmissionEstimate(double[][][] rgbs, int patchRadius, boolean withErosion)
	{		
		// Estimate the atmospheric light
		double[] atmosphericLight = getAtmosphericLight(rgbs, patchRadius);
		
		// Normalize the RGB values with this atmospheric light
		double[][][] normalizedRGBS = new double[rgbs.length][rgbs[0].length][rgbs[0][0].length];
		for(int x = 0; x < rgbs.length; x++) 
		{
			for(int y = 0; y < rgbs[0].length; y++) 
			{
				for(int i = 0; i < rgbs[0][0].length; i++)
				{
					normalizedRGBS[x][y][i] = rgbs[x][y][i] / atmosphericLight[i];
				}
			}
		}
		
		// Generate the dark channel of this normalized RGB values
		double[][] transmissionDarkChannel = getDarkChannel(normalizedRGBS, patchRadius);
		
		// Perform erosion on the transmission if specified
		transmissionDarkChannel = withErosion ? getMaximums(transmissionDarkChannel, patchRadius) : transmissionDarkChannel;		
		
		// Introduce a constant parameter w to keep a very small amount of haze for distant objects
		double w = 0.95;
		
		// Estimate the transmission
		double[][][] transmissionEstimate = new double[rgbs.length][rgbs[0].length][rgbs[0][0].length];
		for(int x = 0; x < transmissionEstimate.length; x++) 
		{
			for(int y = 0; y < transmissionEstimate[0].length; y++) 
			{
				for(int i = 0; i < transmissionEstimate[0][0].length; i++)
				{
					transmissionEstimate[x][y][i] = 1 - w * transmissionDarkChannel[x][y];
					
					// Convert the transmission map to scale from 0-255 for visualization
					transmissionEstimate[x][y][i] *= 255;
				}
			}
		}
				
		return transmissionEstimate;
	}
	
	
	/**
	 * Refine the transmission map
	 * @param rgbs the image's RGB values in array
	 * @param transmissionMap the transmission map
	 * @param patchRadius the patch radius
	 * @return the transmission refine
	 */
	public static double[][][] getTransmissionRefine(double[][][] rgbs, double[][][] transmissionMap, int patchRadius)
	{
		double[][][] transmissionRefine = transmissionMap;
		
		int r = Math.min(rgbs.length, rgbs[0].length)/8;
		double eps = 0.0001;
		
		// Apply Guided filter to perform refinement
		transmissionRefine = new GuidedFilter().process(transmissionRefine, rgbs, r, eps);
		
		return transmissionRefine;
	}
	
	
	/**
	 * Generate the transmission map
	 * @param rgbs the image's RGB values in array
	 * @param patchRadius the patch radius
	 * @param withErosion if the transmission map be eroded
	 * @param withRefinement if the transmission map be refined
	 * @return the transmission map
	 */
	public static double[][][] getTranmissionMap(double[][][] rgbs, int patchRadius, boolean withErosion, boolean withRefinement)
	{
		double[][][] transmissionMap = getTranmissionEstimate(rgbs, patchRadius, withErosion);
		
		// Perform refinement using Guided filter if specified
		transmissionMap = withRefinement ? getTransmissionRefine(rgbs, transmissionMap, patchRadius) : transmissionMap;
		
		return transmissionMap;
	}
	
	
	/**
	 * Recover the scene radiance
	 * @param rgbs the image's RGB values in array
	 * @param patchRadius the patch radius
	 * @param withErosion if the transmission map be eroded
	 * @param withRefinement if the transmission map be refined
	 * @return the recovered radiance
	 */
	public static double[][][] recoverRadiance(double[][][] rgbs, int patchRadius, boolean withErosion, boolean withRefinement)
	{
		// Generate the transmission map
		double[][][] transmissionMap = getTranmissionMap(rgbs, patchRadius, withErosion, withRefinement);
		
		// Generate the atmospheric light estimate
		double[] atmosphericLight = getAtmosphericLight(rgbs, patchRadius);
		
		// Restrict the transmission by lower bound t0 to preserve a small amount of haze in very dense haze regions
		double t0 = 0.1;
		
		// Recover the scene radiance 
		double[][][] radiance = new double[rgbs.length][rgbs[0].length][rgbs[0][0].length];
		for(int x = 0; x < radiance.length; x++) 
		{
			for(int y = 0; y < radiance[0].length; y++) 
			{
				for(int i = 0; i < radiance[0][0].length; i++)
				{
					double I = rgbs[x][y][i];
					double t = transmissionMap[x][y][i]/255 > t0 ? transmissionMap[x][y][i]/255 : t0;
					double A = atmosphericLight[i];
					
					// J = (I - A)/t + A
					radiance[x][y][i] = (I - A)/t + A;
					
					// Clip the value to make sure the it is in range [0, 255]
					radiance[x][y][i] = radiance[x][y][i] > 255 ? 255 : (radiance[x][y][i] < 0 ? 0 : radiance[x][y][i]);
				}
			}
		}
		
		return radiance;
	}
	
	
	/**
	 * Get list of RGB values contained in the patch area with given radius of the pixel(x, y)
	 * @param rgbs the image's RGB values in array
	 * @param x coordinate x of the image
	 * @param y coordinate y of the image
	 * @param patchRadius the patch radius
	 * @return list of RGB values contained in the patch area
	 */
	public static List<double[]> getRGBs(double[][][] rgbs, int x, int y, int patchRadius)
	{	
		// Initialize array to store RGB value of the pixel at the center of the mask
		List<double[]> rgbValues = new ArrayList<double[]>();
		
		// Loop over all pixels under the mask
		for(int i = -patchRadius; i <= patchRadius; i++)
		{
			for(int j = -patchRadius; j <= patchRadius; j++)
			{
				int w = x + i;
				int h = y + j;
				
				// Ignore the pixel that is outside the image's edges
				if(w >= 0 && w < rgbs.length && h >= 0 && h < rgbs[0].length)
				{
					// Get the value of each pixel under the mask
					rgbValues.add(rgbs[w][h]);
				}
			}
		}
		
		return rgbValues;
	}
	
	
	/**
	 * Get an array of the image's RGB values
	 * @param image the input image
	 * @return the image's RGB values in array
	 */
	public static double[][][] getRGBs(BufferedImage image)
	{
		// Initialize array to store RGB value of the pixel at the center of the mask
		double[][][] rgbValues = new double[image.getWidth()][image.getHeight()][3];
		
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{			
				int rgb[] = Processor.getRGB(image, x, y);
				rgbValues[x][y] = new double[] {rgb[0], rgb[1], rgb[2]};
			}
		}
		
		return rgbValues;
	}
	
	
	/**
	 * Perform maximum filter to the 2D array of intensities with given patch radius
	 * @param intensities an array of intensities
	 * @param patchRadius the patch radius
	 * @return an array of maximum values considering intensities under the patch area
	 */
	public static double[][] getMaximums(double[][] intensities, int patchRadius)
	{
		double[][] max = new double[intensities.length][intensities[0].length];
		
		for(int x = 0; x < max.length; x++)
		{
			for(int y = 0; y < max[0].length; y++)
			{
				// Initialize a list to store intensity values under the patch
				List<Double> intensityValues = new ArrayList<Double>();
				
				// Loop over all pixels under the patch
				for(int i = -patchRadius; i <= patchRadius; i++)
				{
					for(int j = -patchRadius; j <= patchRadius; j++)
					{
						int w = x + i;
						int h = y + j;
						
						// Ignore the pixel that is outside the image's edges
						if(w >= 0 && w < intensities.length && h >= 0 && h < intensities[0].length)
						{
							// Get the value of each pixel under the mask
							intensityValues.add(intensities[w][h]);
						}
					}
				}
				
				// Sort the list
				Collections.sort(intensityValues);
				
				// Find the maximum value at the last position in the list 
				max[x][y] = intensityValues.get(intensityValues.size() - 1);
			}
		}
		
		return max;
	}
}


class Pixel implements Comparable<Pixel>
{
	public int posX;
	public int posY;
	public double intensity;
	
	public Pixel(int posX, int posY, double intensity)
	{
		this.posX = posX;
		this.posY = posY;
		this.intensity = intensity;
	}

	@Override
	public int compareTo(Pixel o) 
	{
		return this.intensity > o.intensity ? -1 : this.intensity < o.intensity ? 1 : 0;
	}
}