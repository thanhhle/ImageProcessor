package compression;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class RLCBitPlanesCompressor implements Compressor
{
	private List<Byte>[] compressedImage;
	
	
	/**
	 * Class constructor
	 */
	public RLCBitPlanesCompressor()
	{
		
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void compress(BufferedImage image) 
	{
		// Record the time when the compression starts
		long startTime = System.nanoTime();
		
		// Get the image's bit-planes
		boolean[][] bitPlanes = Compressor.getBitPlanes(image);
		
		// Initialize an array of 9 lists to store the compressed values of the corresponding bit-planes
		this.compressedImage = new ArrayList[10];
		for (int i = 0; i < compressedImage.length; i++) 
        {
			this.compressedImage[i] = new ArrayList<Byte>();
        }
		
		// Store the width and height of the image in the first two arrays
		this.compressedImage[0].addAll(Compressor.intToBytes(image.getWidth()));
		this.compressedImage[1].addAll(Compressor.intToBytes(image.getHeight()));
	
		// Perform RLC on each bit-plane and store the compressed value
		for(int i = 0; i < bitPlanes.length; i++)
		{
			for(int j = 0; j < bitPlanes[0].length; j++)
			{
				// Count the consecutive number of intensities
				int count = 1;
				
				// Assume each bit-planes begins with a white run (1)
				if(j == 0 && !bitPlanes[i][j])
				{
					this.compressedImage[i + 2].add((byte) 0);
				}
				
				while(j < bitPlanes[i].length - 1 && bitPlanes[i][j] == bitPlanes[i][j + 1])
				{
					count++;
					j++;
				}
				
				// Store the number of consecutive number of bit-plane values
				List<Byte> countByte = Compressor.intToBytes(count);
				for(int k = 0; k < countByte.size(); k++)
				{
					if(k == countByte.size() - 1)
					{
						this.compressedImage[i + 2].add(countByte.get(countByte.size() - 1));
					}
					else
					{
						this.compressedImage[i + 2].add((byte) 255);
						
						// Add 0 flipped bits when overflow happens
						this.compressedImage[i + 2].add((byte) 0);
					}
				}			
			}
		}
		
		// Calculate the number of integers take to store the original image and the compressed image
		int originalBits = 0;
		int compressedBits = this.compressedImage[0].size() + this.compressedImage[1].size();	
		for (int i = 2; i < this.compressedImage.length; i++) 
        {
			originalBits += bitPlanes[i - 2].length;
			compressedBits += this.compressedImage[i].size();		
        }

		// Calculate the compression rate
		double compressionRate = (double)originalBits / compressedBits;
		
		// Record the time when the compression ends
		long endTime = System.nanoTime();
		
		// Calculate the runtime of compression
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
		
		// Initialize an array to store the decompressed bit-planes
		boolean[][] bitPlanes = new boolean[8][width * height];
		
		// Decompress the image's bit-planes
		for(int i = 2; i < this.compressedImage.length; i++)
		{	
			// Assume each bit-plane begins with a white run (1)
			boolean bit = true;
			int l = 0;
			for(int j = 0; j < this.compressedImage[i].size(); j++)
			{
				// Change the bit value to 0 if the first bit in the bit-plane is 0
				if(this.compressedImage[i].get(j) == 0)
				{
					bit = !bit;
					j++;
				}
				
				// Get the number of consecutive bit-plane values and add them to the corresponding position
				int count = Byte.toUnsignedInt(this.compressedImage[i].get(j));				
				for(int k = 0; k < count; k++)
				{
					bitPlanes[i - 2][l++] = bit;
				}
				
				// Flip the bit to the opposite one (1 to 0 or 0 to 1)
				bit = !bit;
			}
		}
		
		// Convert bit-planes to intensities and add them to the associated pixels
		int i = 0;
		for(int x = 0; x < decompressedImage.getWidth(); x++)
		{
			for(int y = 0; y < decompressedImage.getHeight(); y++)
			{	
				// Initialize a string to store the bit-planes in binary
				String binaryString = "";
				for(int j = 0; j < bitPlanes.length; j++)
				{
					binaryString += (bitPlanes[j][i] ? "1" : "0");
				}
				
				// Convert the binary value to decimal intensity
				int intensity = Integer.parseInt(binaryString, 2);
				
				decompressedImage.getRaster().setSample(x, y, 0, intensity);
				i++;
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
