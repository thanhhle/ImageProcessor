package processing;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.HistogramDataset;

public interface Processor
{
	/**
	 * Convert an image to grayscale
	 * @param image original image
	 * @return grayscale image
	 */
	public static BufferedImage convertToGrayscale(BufferedImage image)
	{
		BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				// Convert each color attributes (red, green, and blue) to associated grayscale value
				int red = (int) (image.getRaster().getSample(x, y, 0) * 0.299);
				int green = (int) (image.getRaster().getSample(x, y, 1) * 0.587);
				int blue = (int) (image.getRaster().getSample(x, y, 2) * 0.114);
				
				// Set the new color to the pixel(x, y)
				processedImage.getRaster().setSample(x, y, 0, red + green + blue);
            }
         }
		
		return processedImage;	
	}
	
	
	/**
	 * Get the RGB value of pixel(x, y) from the given image
	 * @param image the input image
	 * @param x the x-coordinate of the pixel
	 * @param y the y-coordinate of the pixel
	 * @return the RGB value of the pixel(x, y)
	 */
	public static int[] getRGB(BufferedImage image, int x, int y)
	{
		int[] rgb = new int[3];
		try
		{
			rgb[0] = image.getRaster().getSample(x, y, 0);
			rgb[1] = image.getRaster().getSample(x, y, 1);
			rgb[2] = image.getRaster().getSample(x, y, 2);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			rgb[0] = image.getRaster().getSample(x, y, 0);
			rgb[1] = rgb[0];
			rgb[2] = rgb[0];
		}

		return rgb;
	}
	
	
	/**
	 * Set the RGB value for pixel(x, y) from the given image
	 * @param image the input image
	 * @param x the x-coordinate of the pixel
	 * @param y the y-coordinate of the pixel
	 * @param rgb the RGB value to be set
	 */
	public static void setRGB(BufferedImage image, int x, int y, int[] rgb)
	{
		try
		{
			image.getRaster().setSample(x, y, 0, rgb[0]);
			image.getRaster().setSample(x, y, 1, rgb[1]);
			image.getRaster().setSample(x, y, 2, rgb[2]);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			image.getRaster().setSample(x, y, 0, rgb[0]);
		}
	}
	
	
	/**
	 * Set the RGB value for pixel(x, y) from the given image
	 * @param image the input image
	 * @param x the x-coordinate of the pixel
	 * @param y the y-coordinate of the pixel
	 * @param rgb the RGB value to be set
	 */
	public static void setRGB(BufferedImage image, int x, int y, double[] rgb)
	{
		try
		{
			image.getRaster().setSample(x, y, 0, rgb[0]);
			image.getRaster().setSample(x, y, 1, rgb[1]);
			image.getRaster().setSample(x, y, 2, rgb[2]);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			image.getRaster().setSample(x, y, 0, rgb[0]);
		}
	}
	
	
	/**
	 * Generate a histogram of the given image
	 * @param image the input image
	 */
	public static void generateHistogram(BufferedImage image)
	{
		// Convert image to grayscale if it's not yet in grayscale
    	if(image.getType() != BufferedImage.TYPE_BYTE_GRAY)
		{
    		image = Processor.convertToGrayscale(image);
		}

    	// Initialize an array to store intensities at every pixels in the image
    	double intensities[] = new double[image.getWidth() * image.getHeight()];

    	// Loop through the image and store the intensities to the arrray
		int i = 0;
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{					
				intensities[i++] = image.getRaster().getSample(x, y, 0);
			}
		}
		
		// Generate a histogram dataset from the array
		HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Intensity Level", intensities, 256);
 
        // Generate a histogram from that dataset 
        JFreeChart chart = ChartFactory.createHistogram("Histogram", "Intensity", "Frequency", dataset);
        ChartFrame frame = new ChartFrame("Result", chart);
        frame.pack();
        frame.setVisible(true);
	}
	
	
	/**
	 * Clip the RGB value to make sure it's in range [0, 255]
	 * @param rgb the input RGB value
	 * @return the clipped RGB value
	 */
	public static int[] clipRGB(int[] rgb)
	{
		for(int i = 0; i < rgb.length; i++)
		{
			rgb[i] = rgb[i] > 255 ? 255 : (rgb[i] < 0 ? 0 : rgb[i]);
		}
		return rgb;
	}
	
	
	
	/**
	 * Get list of RGB values contained in the neighborhood size m x n of the pixel(x, y)
	 * @param image the input image
	 * @param x coordinate x of the image
	 * @param y coordinate y of the image
	 * @param m width of the neighborhood
	 * @param n height of the neighborhood
	 * @return list of RGB values contained in the neighborhood
	 */
	public static List<int[]> getRGBs(BufferedImage image, int x, int y, int subimageWidth, int subimageHeight)
	{
		// Initialize array to store RGB value of the pixel at the center of the mask
		List<int[]> rgbValues = new ArrayList<int[]>();
		
		// Loop over all pixels under the mask
		for(int i = -subimageWidth/2; i <= subimageWidth/2; i++)
		{
			for(int j = -subimageHeight/2; j <= subimageHeight/2; j++)
			{
				int w = x + i;
				int h = y + j;
				
				// Ignore the pixel that is outside the image's edges
				if(w >= 0 && w < image.getWidth() && h >= 0 && h < image.getHeight())
				{
					// Get the value of each pixel under the mask
					rgbValues.add(Processor.getRGB(image, w, h));
				}
			}
		}
		
		return rgbValues;
	}
	
	
	
	/**
	 * Calculate the Root Mean Square Error of the processed image and the original image
	 * @param originalImage the original image
	 * @param processedImage the processed image
	 */
	public static void calculateRMSE(BufferedImage originalImage, BufferedImage processedImage)
	{
		double rmse = 0.0;

        for (int x = 0; x < originalImage.getWidth(); x++)
        {
            for (int y = 0; y < originalImage.getHeight(); y++) 
            {
                int originalIntensity = originalImage.getRaster().getSample(x, y, 0);
                int processedIntensity = processedImage.getRaster().getSample(x, y, 0);
                
                rmse += Math.pow(processedIntensity - originalIntensity, 2);
            }
        }

        rmse /= (originalImage.getWidth() * originalImage.getHeight());
        rmse = Math.sqrt(rmse);
        
        JFrame result = new JFrame("Result"); 
		result.setSize(300, 150);  
		result.setLocationRelativeTo(null);  
		result.getContentPane().setLayout(null);
		result.setVisible(true);   

	    JLabel rmseLabel = new JLabel("RMSE: ");
	    rmseLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    rmseLabel.setBounds(100, 30, 200, 25);
	    result.add(rmseLabel);  
	    
	    JLabel rmseValue = new JLabel(String.valueOf(rmse));
	    rmseValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    rmseValue.setBounds(150, 30, 200, 25);
	    result.add(rmseValue); 
	}
}
