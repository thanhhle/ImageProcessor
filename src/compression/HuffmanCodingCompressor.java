package compression;

import java.awt.image.BufferedImage;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;


public class HuffmanCodingCompressor implements Compressor
{
	private BitSet[] compressedImage;
	private Map<Byte, Code> huffmanCodes;
	
	
	/**
	 * Class constructor
	 */
	public HuffmanCodingCompressor()
	{
		
	}

	
	@Override
	public void compress(BufferedImage image) 
	{
		long startTime = System.nanoTime();
		
		// Initialize maps to store the encoding and decoding
		this.huffmanCodes = new HashMap<Byte, Code>();
		
		// Count the frequency of all intensities in the image
		int[] freq = this.getIntensityFreq(image);
		
		// Build Huffman tree
		Node root = buildTree(freq);
		
		// Set the Huffman code for the root
		BitSet rootBitSet = new BitSet();
		Code rootCode = new Code(rootBitSet, rootBitSet);
		
		// Generate Huffman code
		this.generateHuffmanCode(root, rootCode);

		// Initialize a BitSet array to store the compressed values
		this.compressedImage = new BitSet[3];
		
		// Store the width and height of the image in the first two arrays
		this.compressedImage[0] = Compressor.intToBitSet(image.getWidth());
		this.compressedImage[1] = Compressor.intToBitSet(image.getHeight());
		
		// Encode the image and calculate the number of bytes take to store the original image and the compressed image
		int originalBits = image.getWidth() * image.getHeight() * 8;
		int compressedBits = this.encode(image) + this.compressedImage[0].length() + this.compressedImage[1].length();
		
		// Calculate the compression rate
		double compressionRatio = (double)originalBits / compressedBits;
		
		// Record the time when the compression ends
		long endTime = System.nanoTime();
		
		// Calculate the runtime of the compression
		long runTime = endTime - startTime;
		
		// Show the compression result
		Compressor.showCompressionResult(compressionRatio, runTime, this);
	}


	@Override
	public void decompress() 
	{	
		// Record the time when the decompression starts
		long startTime = System.nanoTime();
		
		// Initialize a BufferedImage object to store the decompressed image
		int width = Compressor.bitSetToInt(compressedImage[0]);
		int height = Compressor.bitSetToInt(compressedImage[1]);
		BufferedImage decompressedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		// Construct the decoding map which the swapped key and value version of Huffman codes
		Map<Code, Byte> decodingMap = this.huffmanCodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		int i = 0;
		for(int x = 0; x < decompressedImage.getWidth(); x++)
		{
			for(int y = 0; y < decompressedImage.getHeight(); y++)
			{
				BitSet length = new BitSet();
				Code code = null;
				
				// Keep increasing the length of bits used and compare with the decoding map to until a recorded code is found
				do
				{
					// Increase the length of bits considered by 1
					length.set(length.length());
					
					// Get the next "length" bits from BitSet starting at i
					BitSet bitSet = (BitSet) length.clone();
					bitSet.and(this.compressedImage[2].get(i, i + length.length()));
					
					// Construct a Huffman code with the length and bit set to check if it is in the decoding map
					code = new Code(length, bitSet);
				}
				while(decodingMap.get(code) == null);
				
				// Update the index position where the decoding has been done
				i += length.length();
				
				// Get the intensity from the decoding map with detected Huffman code
				int intensity = Byte.toUnsignedInt(decodingMap.get(code));
				
				// Set the intensity to the pixel(x, y)
				decompressedImage.getRaster().setSample(x, y, 0, intensity);
			}
		}
		
		// Record the time when the decompression ends
		long endTime = System.nanoTime();
		
		// Calculate the runtime of the decompression
		long runTime = endTime - startTime;
		
		// Show the decompression result
		Compressor.showDecompressionResult(runTime, decompressedImage);
	}
	
	
	/**
	 * Calculate the frequency of 256 intensities (0-225)
	 * @param image the input image
	 * @return an array of intensity frequencies
	 */
	private int[] getIntensityFreq(BufferedImage image)
	{
		int[] freq = new int[256];
	
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				int intensity = image.getRaster().getSample(x, y, 0);
				freq[intensity]++;
			}
		}
		
		return freq;
	}
	
	
	/**
	 * Build the Huffman tree
	 * @param freq an array of intensity frequencies
	 * @return the root node
	 */
	private Node buildTree(int[] freq)
	{
		// Initialize a priority queue to store the nodes of the tree in ascending order
		PriorityQueue<Node> priorityQueue = new PriorityQueue<Node>(Comparator.comparingInt(node -> node.freq));
		for(int i = 0; i < freq.length; i++)
		{
			// Create a leaf node for each intensities appear in the image and add it to the priority queue
			if(freq[i] > 0)
			{
				priorityQueue.add(new Node(i, freq[i], null, null));
			}		
		}
		
		// Repeat the process until there is only one node the queue
		while(priorityQueue.size() > 1)
		{
			// Remove two nodes with the smallest frequencies
			Node left = priorityQueue.poll();
			Node right = priorityQueue.poll();
			
			// Calculate the sum of frequencies of these nodes
			int sum = left.freq + right.freq;
			
			// Generate an intermediate node as their parent with the sum value
			// Assign these nodes as the child nodes of this parent node
			Node parent = new Node(-1, sum, left, right);
			
			// Add the parent node back to the queue
			priorityQueue.add(parent);
		}
		
		// Return the root of the tree
		return priorityQueue.peek();
	}
	
	
	/**
	 * Generate the Huffman code for the children nodes of given parent node 
	 * @param node the parent node
	 * @param code the Huffman code of the parent node
	 */
	private void generateHuffmanCode(Node node, Code code)
	{	
		if(node.leftNode == null && node.rightNode == null)
		{
			// Record the code and intensity to the map if the node is a leaf		
			this.huffmanCodes.put((byte)node.intensity, code);
		}
		else
		{
			// Increase the length of number of bits by 1
			BitSet length = (BitSet) code.length.clone();
			length.set(length.length());
			
			// Add "0" to the bit set of left node
			BitSet leftBitSet = (BitSet) code.bitSet.clone();
			Code left = new Code(length, leftBitSet);
			
			// Add "1" to the bit set of right node
			BitSet rightBitSet = (BitSet) code.bitSet.clone();
			rightBitSet.set(code.length.length());
			Code right = new Code(length, rightBitSet);
			
			// Recursively call the left node
			generateHuffmanCode(node.leftNode, left);
		
			// Recursively call the right node
			generateHuffmanCode(node.rightNode, right);
		}
	}
	
	
	/**
	 * Encode the image using Huffman codes
	 * @param image the input image
	 * @return the number of bits used to store compressed image
	 */
	private int encode(BufferedImage image)
	{
		// Initialize the BitSet object to store the compressed image
		BitSet bitSet = new BitSet();
		
		// Initialize an object to store the number of bits used
		int bitCount = 0;
		
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				// Get the intensity if the image at (x, y)
				byte intensity = (byte) image.getRaster().getSample(x, y, 0);
				
				// Get the code associated with this intensity from the encoding map
				Code code = this.huffmanCodes.get(intensity);
				
				// Set the bit in the BitSet object and increase the count of bits
				for(int i = 0; i < code.length.length(); i++) 
				{
					if(code.bitSet.get(i))
					{
						bitSet.set(bitCount++);
					}
					else
					{
						bitCount++;
					}
				}
			}
		}
		
		// Store the BitSet object in the third array
		this.compressedImage[2] = bitSet;
		
		// Return the number of bits used
		return bitCount;
	}
}


class Node
{
	public int intensity;
	public int freq;
	public Node leftNode;
	public Node rightNode;
	public Code code;
	
	public Node(int intensity, int freq, Node leftNode, Node rightNode)
	{
		this.intensity = intensity;
		this.freq = freq;
		this.leftNode = leftNode;
		this.rightNode = rightNode;
	}
}


class Code
{
	public BitSet length;
	public BitSet bitSet;
	
	public Code(BitSet length, BitSet bitSet)
	{
		this.length = length;
		this.bitSet = bitSet;
	}

	@Override
	public boolean equals(Object o) 
	{
		Code c = (Code) o;
		return (this.length.equals(c.length) && this.bitSet.equals(c.bitSet));
	}
	
	@Override public int hashCode()
	{ 
		return (int) length.hashCode() * bitSet.hashCode();
	}
}

