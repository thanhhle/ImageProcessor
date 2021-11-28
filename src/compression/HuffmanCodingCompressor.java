package compression;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import processing.Processor;


public class HuffmanCodingCompressor implements Compressor
{
	private List<BitSet>[] compressedImage;
	private Map<BitSet, Byte> huffmanCode;
	
	private double compressionRate;
	
	
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
		
		Map<Byte, Integer> freq = this.getIntensityFreq(image);
		
		PriorityQueue<Node> priorityQueue = new PriorityQueue<Node>(Comparator.comparingInt(f -> f.freq));
		for(Byte key: freq.keySet())
		{
			priorityQueue.add(new Node(key, freq.get(key)));
		}
	
		while(priorityQueue.size() != 1)
		{
			Node leftNode = priorityQueue.poll();
			Node rightNode = priorityQueue.poll();
			
			int sum = leftNode.freq + rightNode.freq;
			priorityQueue.add(new Node((byte) 0, sum, leftNode, rightNode));
		}
		
		Node root = priorityQueue.peek();
		BitSet rootBitSet = new BitSet(1);
		rootBitSet.set(0);
		
		this.generateHuffmanCode(root, new BitSet(1));
		
		BitSet width = intToBitSet(image.getWidth());
		BitSet height = intToBitSet(image.getHeight());

		this.compressedImage[0].add(width);
		this.compressedImage[1].add(height);
		
		// this.compressionRate = intensities.length / compressedImage.size();
		
		long endTime = System.nanoTime();
		long runTime = endTime - startTime; 
		
		Compressor.showCompressionResult(this.compressionRate, runTime, this);
	}


	@Override
	public void decompress() 
	{
		
		
	}
	
	
	private Map getIntensityFreq(BufferedImage image)
	{
		Map<Byte, Integer> freq = new HashMap<Byte, Integer>();
		
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				byte intensity = (byte) image.getRaster().getSample(x, y, 0);
				freq.put(intensity, freq.getOrDefault(intensity, 0) + 1);
			}
		}
		
		return freq;
	}
	
	
	private void generateHuffmanCode(Node root, BitSet bitSet)
	{
		if(root == null)
		{
			return;
		}
		
		if(root.leftNode == null && root.rightNode == null)
		{
			if(bitSet.length() == 0)
			{
				bitSet.set(0);
			}
			
			huffmanCode.put(bitSet, root.intensity);
		}
		
		BitSet leftBitSet = new BitSet(bitSet.length() + 1);
		leftBitSet.or(bitSet);
		
		BitSet rightBitSet = new BitSet(bitSet.length() + 1);
		rightBitSet.or(leftBitSet);
		rightBitSet.set(rightBitSet.length() - 1);
		
		generateHuffmanCode(root.leftNode, leftBitSet);
		generateHuffmanCode(root.rightNode, rightBitSet);
	}
	
	
	private void encode(BufferedImage image)
	{
		BitSet bit = new BitSet();
		for(int x = 0; x < image.getWidth(); x++)
		{
			for(int y = 0; y < image.getHeight(); y++)
			{
				
			}
		}
	}
	
	
	public static BitSet intToBitSet(int value) 
	{
        BitSet bits = new BitSet();
        
        int i = 0;
        while (value != 0) 
        {
            if (value % 2 != 0) 
            {
                bits.set(i);
            }
            
            i++;
            value = value >>> 1;
        }

        return bits;
    }
}


class Node implements Comparable<Node>
{
	public byte intensity;
	public int freq;
	public Node leftNode;
	public Node rightNode;
	
	public Node(byte intensity, int probability)
	{
		this.intensity = intensity;
		this.freq = probability;
	}
	
	public Node(byte intensity, int freq, Node leftNode, Node rightNode)
	{
		this(intensity, freq);
		this.leftNode = leftNode;
		this.rightNode = rightNode;
	}


	@Override
	public int compareTo(Node o) 
	{
		return (int)(this.freq - o.freq);
	}
}

class Code implements Comparable<Code>
{
	public BitSet code;
	public byte intensity;
	
	public Code(BitSet code, byte intensity)
	{
		this.code = code;
		this.intensity = intensity;
	}


	@Override
	public int compareTo(Code o) 
	{
		return this.code.length() > o.code.length() ? -1 : this.code.length() < o.code.length() ? 1 : 0;
	}
}
