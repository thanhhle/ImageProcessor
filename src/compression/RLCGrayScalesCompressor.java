package compression;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class RLCGrayScalesCompressor implements Compressor
{
	private List<Byte>[] compressedImage;
	
	
	/**
	 * Class constructor
	 */
	public RLCGrayScalesCompressor()
	{
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void compress(BufferedImage image) 
	{
		// Record the time when the compression starts
		long startTime = System.nanoTime();
		
		// Get the image's intensity values
		byte[] intensities = Compressor.getIntensities(image);
		
		// Initialize a byte array to store the compressed values
		this.compressedImage = new ArrayList[3];
		for (int i = 0; i < compressedImage.length; i++) 
        {
        	compressedImage[i] = new ArrayList<Byte>();
        }
		
		// Store the width and height of the image in the first two arrays
		this.compressedImage[0].addAll(Compressor.intToBytes(image.getWidth()));
		this.compressedImage[1].addAll(Compressor.intToBytes(image.getHeight()));
		
		// Perform RLC on the intensities and store the compressed value
		for(int i = 0; i < intensities.length; i++)
		{
			// Count the consecutive number of intensities
			int count = 1;
			while(i < intensities.length - 1 && intensities[i] == intensities[i + 1])
			{
				count++;
				i++;
			}
			
			// Store the number of consecutive number of intensities at position k
			// Store the intensity value at position k + 1
			List<Byte> countByte = Compressor.intToBytes(count);
			byte intensity = intensities[i];
			
			for(int j = 0; j < countByte.size(); j++)
			{
				this.compressedImage[2].add(j == countByte.size() - 1 ? (byte)(countByte.get(countByte.size() - 1)) : -1);
				this.compressedImage[2].add(intensity);
			}
		}
		
		
		// Calculate the number of bytes take to store the original image and the compressed image
		int originalBits = intensities.length;
		int compressedBits = this.compressedImage[0].size() + this.compressedImage[1].size() + this.compressedImage[2].size();
	
		// Calculate the compression rate
		double compressionRate = (double)originalBits / compressedBits;
		
		// Record the time when the compression ends
		long endTime = System.nanoTime();
		
		// Calculate the runtime of the compression
		long runTime = endTime - startTime;
		
		// Show the compression result
		Compressor.showCompressionResult(compressionRate, runTime, this);
	}


	@Override
	public void decompress() 
	{
		// Record the time when the decompression starts
		long startTime = System.nanoTime();
	
		// Initialize a BufferedImage object to store the decompressed image
		int width = Compressor.bytesToInt(compressedImage[0]);
		int height = Compressor.bytesToInt(compressedImage[1]);
		BufferedImage decompressedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		// Perform the decompression
		int i = 0;
		int x = 0;
		int y = 0;
		while(x < width && y < height)
		{
			// The number of consecutive pixels is stored at i
			int count = Byte.toUnsignedInt(this.compressedImage[2].get(i++));
			
			// The intensity value is stored at i + 1
			int intensity = Byte.toUnsignedInt(this.compressedImage[2].get(i++));
			
			// Loop through the number of consecutive pixels and set the intensity values to the associated pixels
			for(int j = 0; j < count; j++)
			{
				decompressedImage.getRaster().setSample(x, y, 0, intensity);
				y++;
				
				if(y == decompressedImage.getHeight())
				{
					y = 0;
					x++;
				}
			}
			
			// Stop the process when all the pixels are filled
			if(x == decompressedImage.getWidth())
			{
				break;
			}
		}
		
		// Record the time when the decompression ends
		long endTime = System.nanoTime();
		
		// Calculate the runtime of the decompression
		long runTime = endTime - startTime;
		
		// Show the decompression result
		Compressor.showDecompressionResult(runTime, decompressedImage);
	}
}
