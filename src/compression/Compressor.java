package compression;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import main.MainFrame;

public interface Compressor 
{	
	/**
	 * Perform compression on the given image
	 * @param image the input image
	 */
	public void compress(BufferedImage image);
	
	
	/**
	 * Perform decompression on the compressed image
	 */
	public void decompress();
	
	
	/**
	 * Get an array of the image's intensity values in byte
	 * @param image the input image
	 * @return an byte array of intensity values
	 */
	public static byte[] getIntensities(BufferedImage image)
	{
		byte[] intensities = new byte[image.getWidth() * image.getHeight()];
		
		int i = 0;
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				intensities[i] = (byte)(image.getRaster().getSample(x, y, 0));
				i++;
			}
		}
		return intensities;
	}
	
	
	/**
	 * Get a 2D array of image's 8 bit-plane values
	 * @param image the input image
	 * @return array of bit-plane values
	 */
	public static boolean[][] getBitPlanes(BufferedImage image)
	{
		boolean[][] bitPlanes = new boolean[8][image.getWidth() * image.getHeight()];
		
		int i = 0;
		for(int x = 0; x < image.getWidth(); x++) 
		{
			for(int y = 0; y < image.getHeight(); y++) 
			{
				int intensity = image.getRaster().getSample(x, y, 0);
				
				String binaryString = String.format("%8s", Integer.toBinaryString(intensity)).replace(' ', '0');
				for(int j = 0; j < binaryString.length(); j++)
				{
					if(binaryString.charAt(j) == '1')
					{
						bitPlanes[j][i] = true;
					}
				}
				
				i++;
			}
		}
		return bitPlanes;
	}
	
	
	/**
	 * Show the compression rate with compression rate and compression time
	 * Click on "DECOMPRESS" button to decompress the image
	 * @param compressionRatio the compression ratio
	 * @param compressionTime the compression time
	 * @param compressor the compressor used to compress the image
	 */
	public static void showCompressionResult(double compressionRatio, long compressionTime, Compressor compressor)
	{
		JFrame result = new JFrame("Result"); 
		result.setSize(300, 150);  
		result.setLocationRelativeTo(null);  
		result.getContentPane().setLayout(null);
		result.setVisible(true);   
	    
	    JLabel ratioLabel = new JLabel("Compression Ratio: ");
	    ratioLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    ratioLabel.setBounds(40, 10, 200, 25);
	    result.add(ratioLabel);  
	    
	    JLabel ratioValue = new JLabel(String.valueOf(String.format("%.4f", compressionRatio)));
	    ratioValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    ratioValue.setBounds(170, 10, 200, 25);
	    result.add(ratioValue);
	    
	    JLabel timeLabel = new JLabel("Compression Time (ns): ");
	    timeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    timeLabel.setBounds(40, 40, 200, 25);
	    result.add(timeLabel);  
	    
	    JLabel timeValue = new JLabel(String.valueOf(compressionTime));
	    timeValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    timeValue.setBounds(170, 40, 200, 25);
	    result.add(timeValue); 
	    
	    JButton decompressButton = new JButton("DECOMPRESS");  
	    decompressButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    decompressButton.setBounds(40, 70, 220, 30);
	    result.add(decompressButton);
		
	    decompressButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				result.dispose();
				compressor.decompress();
			}
		});
	}
	
	
	/**
	 * Show the decompression result with decompression time
	 * Click on "SHOW DECOMPRESSED IMAGE" button to show the decompressed image on the Processed Image space
	 * @param decompressionTime the decompression time
	 * @param decompressedImage the decompressed image
	 */
	public static void showDecompressionResult(long decompressionTime, BufferedImage decompressedImage)
	{
		JFrame result = new JFrame("Result"); 
		result.setSize(300, 150);  
		result.setLocationRelativeTo(null);  
		result.getContentPane().setLayout(null);
		result.setVisible(true);   

	    JLabel timeLabel = new JLabel("Decompression Time (ns): ");
	    timeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    timeLabel.setBounds(40, 30, 200, 25);
	    result.add(timeLabel);  
	    
	    JLabel time = new JLabel(String.valueOf(decompressionTime));
	    time.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    time.setBounds(170, 30, 200, 25);
	    result.add(time); 
	    
	    JButton showDecompressedImageButton = new JButton("SHOW DECOMPRESSED IMAGE");  
	    showDecompressedImageButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
	    showDecompressedImageButton.setBounds(40, 70, 220, 30);
	    result.add(showDecompressedImageButton);
		
	    showDecompressedImageButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				result.dispose();
				MainFrame.processedImg = decompressedImage;
				MainFrame.processedImage.setIcon(new ImageIcon(decompressedImage));
			}
		});
	}
	
	
	public static List<Byte> intToBytes(int value)
	{
		List<Byte> bytes = new ArrayList<Byte>();
		
		while(value > 255)
		{
			bytes.add((byte) 255);
			value -= 255;
		}
		
		bytes.add((byte) value);
	
		return bytes;
	}
	
	
	public static int bytesToInt(List<Byte> bytes)
	{
		int value = (bytes.size() - 1) * 255;
		value += Byte.toUnsignedInt(bytes.get(bytes.size() - 1));
		
		return value;
	}
	
	
	public static BitSet intToBitSet(int value) 
	{
        BitSet bitSet = new BitSet();
        
        int i = 0;
        while (value != 0) 
        {
            if (value % 2 != 0) 
            {
            	bitSet.set(i);
            }
            
            i++;
            value = value >>> 1;
        }

        return bitSet;
    }
	
	
	public static int bitSetToInt(BitSet bitSet)
	{
		int intValue = 0;
        for (int bit = 0; bit < bitSet.length(); bit++) 
        {	
            if (bitSet.get(bit)) 
            {
                intValue |= (1 << bit);
            }
        }
        return intValue;
	}
}
