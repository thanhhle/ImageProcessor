package main;

import interpolation.Interpolator;
import interpolation.NearestNeighborInterpolator;
import processing.BitPlanesRemover;
import processing.HistogramEqualizator;
import processing.PixelSizeConverter;
import processing.Processor;
import interpolation.LinearInterpolator;
import interpolation.BilinearInterpolator;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

import compression.Compressor;
import compression.HuffmanCodingCompressor;
import compression.RLCBitPlanesCompressor;
import compression.RLCGrayScalesCompressor;
import denoising.AlphaTrimmedMeanFilter;
import denoising.ArithmeticMeanFilter;
import denoising.ContraharmonicMeanFilter;
import denoising.DenoisingFilter;
import denoising.GeometricMeanFilter;
import denoising.HarmonicMeanFilter;
import denoising.MaxFilter;
import denoising.MidpointFilter;
import denoising.MinFilter;
import filtering.AveragingFilter;
import filtering.GaussianFilter;
import filtering.HighBoostingFilter;
import filtering.ImageSharpeningFilter;
import filtering.LaplacianFilter;
import filtering.MedianFilter;
import filtering.SpatialFilter;
import hazeRemoval.DarkChannel;
import hazeRemoval.HazeRemovalFilter;
import hazeRemoval.HazeRemover;
import hazeRemoval.TransmissionMap;
import imnoising.GaussianNoiseFilter;
import imnoising.ImnoisingFilter;
import imnoising.PoissonNoiseFilter;
import imnoising.SaltAndPepperNoiseFilter;
import imnoising.SpeckleNoiseFilter;

import javax.swing.SpinnerNumberModel;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;

public class MainFrame extends JFrame
{
	private JPanel contentPane;
	
	private BufferedImage originalImg = null;
	public static BufferedImage processedImg = null;
	
	private JLabel originalImage;
	public static JLabel processedImage;
	
	private JComboBox<String> scalingAlgorithms;
	private JComboBox<String> filteringOperations;
	private JComboBox<String> histogramEqualizationOptions;
	private JComboBox<String> noiseTypes;
	private JComboBox<String> compressionMethods;
	
	private JSpinner imageWidth;
	private JSpinner imageHeight;
	private JSpinner pixelSize;
	private JSpinner heKernelRadius;
	private JSpinner filterKernelRadius;
	private JSpinner filterSubimageWidth;
	private JSpinner filterSubimageHeight;
	private JSpinner hazeRemovalPatchRadius;
	
	private JSpinner standardDeviation;
	private JSpinner kValue;
	private JSpinner qValue;
	private JSpinner dValue;
	private JSpinner varianceGaussian;
	private JSpinner mean;
	private JSpinner density;
	private JSpinner varianceSpeckle;
	
	private JCheckBox withErosion;
	private JCheckBox withRefinement;
	
	private ButtonGroup laplacianButtonGroup;
	
	private JCheckBox[] bitPlanes = new JCheckBox[8];
	
	private List<Integer> removedBitPlanes;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try 
				{
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame()
	{
		setTitle("Image Processor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 1400, 670);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		originalImage = new JLabel("");
		originalImage.setHorizontalTextPosition(SwingConstants.CENTER);
		originalImage.setHorizontalAlignment(SwingConstants.CENTER);
		originalImage.setBounds(25, 50, 512, 512);
		originalImage.setBorder(new LineBorder(Color.BLACK));
		contentPane.add(originalImage);
		
		originalImage.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					double widthRatio = (double)originalImage.getWidth()/originalImg.getWidth();
				    double heightRatio = (double)originalImage.getHeight()/originalImg.getHeight();
				    double ratio = Math.min(widthRatio, heightRatio);
				    
				    originalImg = new BilinearInterpolator().process(originalImg, (int)(originalImg.getWidth() * ratio), (int)(originalImg.getHeight() * ratio));		 
					setOriginalImage(originalImg);
				}
			}
		});
		
		
		processedImage = new JLabel("");
		processedImage.setHorizontalTextPosition(SwingConstants.CENTER);
		processedImage.setHorizontalAlignment(SwingConstants.CENTER);
		processedImage.setBounds(850, 50, 512, 512);
		processedImage.setBorder(new LineBorder(Color.BLACK));
		contentPane.add(processedImage);
		
		processedImage.addMouseListener(new MouseAdapter()  
		{  
		    public void mousePressed(MouseEvent e)  
		    {  
		    	processedImage.setIcon(new ImageIcon(originalImg));
		    }
		    
		    
		    public void mouseReleased(MouseEvent e)  
		    {
		    	processedImage.setIcon(new ImageIcon(processedImg));
		    }
		}); 
		
		JLabel originalImageLabel = new JLabel("Original Image");
		originalImageLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		originalImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		originalImageLabel.setBounds(25, 8, 512, 25);
		contentPane.add(originalImageLabel);
		
		JLabel processedImageLabel = new JLabel("Processed Image");
		processedImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		processedImageLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		processedImageLabel.setBounds(850, 8, 512, 25);
		contentPane.add(processedImageLabel);
		
		JLabel scalingAlgorithmLabel = new JLabel("Scaling Algorithm");
		scalingAlgorithmLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scalingAlgorithmLabel.setBounds(560, 50, 110, 25);
		contentPane.add(scalingAlgorithmLabel);
		
		// Scaling
		scalingAlgorithms = new JComboBox(new String[]{"-", "Nearest Neighbor", "Linear X", "Linear Y", "Bilinear"});
		scalingAlgorithms.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scalingAlgorithms.setBounds(675, 50, 150, 25);
		contentPane.add(scalingAlgorithms);
		
		JLabel imageSizeLabel = new JLabel("Image Size");
		imageSizeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		imageSizeLabel.setBounds(560, 90, 110, 25);
		contentPane.add(imageSizeLabel);
		
		imageWidth = new JSpinner();
		imageWidth.setModel(new SpinnerNumberModel(0, 0, null, 1));
		imageWidth.setBounds(675, 90, 65, 25);
		contentPane.add(imageWidth);
		
		imageHeight = new JSpinner();
		imageHeight.setModel(new SpinnerNumberModel(0, 0, null, 1));
		imageHeight.setBounds(760, 90, 65, 25);
		contentPane.add(imageHeight);
		
		JLabel scalingXLabel = new JLabel("x");
		scalingXLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scalingXLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scalingXLabel.setBounds(743, 90, 15, 25);
		contentPane.add(scalingXLabel);
		
		
		// Pixel Size
		JLabel pixelSizeLabel = new JLabel("Pixel Size (1 - 8)");
		pixelSizeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		pixelSizeLabel.setBounds(560, 150, 110, 25);
		contentPane.add(pixelSizeLabel);
		
		pixelSize = new JSpinner();
		pixelSize.setModel(new SpinnerNumberModel(8, 1, 8, 1));
		pixelSize.setBounds(675, 150, 150, 25);
		contentPane.add(pixelSize);
		
		
		// Histogram Equalization
		JLabel histogramEqualizationLabel = new JLabel("Histogram Equalization");
		histogramEqualizationLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		histogramEqualizationLabel.setBounds(560, 210, 110, 25);
		contentPane.add(histogramEqualizationLabel);
		
		histogramEqualizationOptions = new JComboBox(new String[]{"-", "Global", "Local"});
		histogramEqualizationOptions.setFont(new Font("Tahoma", Font.PLAIN, 11));
		histogramEqualizationOptions.setBounds(675, 210, 150, 25);
		contentPane.add(histogramEqualizationOptions);
		
		JLabel heKernelRadiusLabel = new JLabel("HE Kernel Radius");
		heKernelRadiusLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		heKernelRadiusLabel.setBounds(560, 250, 110, 25);
		heKernelRadiusLabel.setVisible(false);
		contentPane.add(heKernelRadiusLabel);
		
		heKernelRadius = new JSpinner();
		heKernelRadius.setModel(new SpinnerNumberModel(1, 1, null, 1));
		heKernelRadius.setBounds(675, 250, 150, 25);
		heKernelRadius.setVisible(false);
		contentPane.add(heKernelRadius);
		
		histogramEqualizationOptions.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e) 
		    {	
				heKernelRadiusLabel.setVisible(histogramEqualizationOptions.getSelectedItem().toString().equals("Local"));
				heKernelRadius.setVisible(histogramEqualizationOptions.getSelectedItem().toString().equals("Local"));
		    }
		});
		
		
		// Filtering Operation
		JLabel operationLabel = new JLabel("Operation");
		operationLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		operationLabel.setBounds(560, 310, 110, 25);
		contentPane.add(operationLabel);
		
		filteringOperations = new JComboBox(new String[]{"-", "Averaging Filter", "Gaussian Filter", "Median Filter", 
															  "Laplacian Filter", "Image Sharpening Filter", "High-boosting Filter",
															  "Arithmetic Mean Denoising", "Geometric Mean Denoising", "Harmonic Mean Denoising", "Contraharmonic Mean Denoising", "Max Denoising", "Min Denoising", "Midpoint Denoising", "Alpha-trimmed Mean Denoising",
															  "Gaussian Noise", "Poisson Noise", "Salt & Pepper Noise", "Speckle Noise",
															  "Dark Channel", "Transmission Map", "Haze Removal"});
		filteringOperations.setFont(new Font("Tahoma", Font.PLAIN, 11));
		filteringOperations.setBounds(675, 310, 150, 25);
		contentPane.add(filteringOperations);
		
		JLabel filterSizeLabel = new JLabel();
		filterSizeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		filterSizeLabel.setBounds(560, 350, 110, 25);
		filterSizeLabel.setVisible(false);
		contentPane.add(filterSizeLabel);
		
		filterKernelRadius = new JSpinner();
		filterKernelRadius.setModel(new SpinnerNumberModel(1, 1, null, 1));
		filterKernelRadius.setBounds(675, 350, 150, 25);
		filterKernelRadius.setVisible(false);
		contentPane.add(filterKernelRadius);
		
		filterSubimageWidth = new JSpinner();
		filterSubimageWidth.setModel(new SpinnerNumberModel(3, 3, null, 2));
		filterSubimageWidth.setBounds(675, 350, 65, 25);
		filterSubimageWidth.setVisible(false);
		contentPane.add(filterSubimageWidth);
		
		filterSubimageHeight = new JSpinner();
		filterSubimageHeight.setModel(new SpinnerNumberModel(3, 3, null, 2));
		filterSubimageHeight.setBounds(760, 350, 65, 25);
		filterSubimageHeight.setVisible(false);
		contentPane.add(filterSubimageHeight);
		
		hazeRemovalPatchRadius = new JSpinner();
		hazeRemovalPatchRadius.setModel(new SpinnerNumberModel(7, 3, null, 2));
		hazeRemovalPatchRadius.setBounds(675, 350, 150, 25);
		hazeRemovalPatchRadius.setVisible(false);
		contentPane.add(hazeRemovalPatchRadius);
		
		JLabel filterXLabel = new JLabel("x");
		filterXLabel.setHorizontalAlignment(SwingConstants.CENTER);
		filterXLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		filterXLabel.setBounds(743, 350, 15, 25);
		filterXLabel.setVisible(false);
		contentPane.add(filterXLabel);
		

		// Standard Deviation - Gaussian Filter
		JLabel standardDeviationLabel = new JLabel("Standard Deviation \u03C3");
		standardDeviationLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		standardDeviationLabel.setBounds(560, 390, 110, 25);
		standardDeviationLabel.setVisible(false);
		contentPane.add(standardDeviationLabel);
		
		standardDeviation = new JSpinner();
		standardDeviation.setModel(new SpinnerNumberModel(1.0, null, null, 1.0));
		standardDeviation.setBounds(675, 390, 150, 25);
		standardDeviation.setVisible(false);
		contentPane.add(standardDeviation);
		
		// Value of k - High-boosting Filter
		JLabel kValueLabel = new JLabel("Value of k");
		kValueLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		kValueLabel.setBounds(560, 390, 110, 25);
		kValueLabel.setVisible(false);
		contentPane.add(kValueLabel);
		
		kValue = new JSpinner();
		kValue.setModel(new SpinnerNumberModel(1.0, null, null, 1.0));
		kValue.setBounds(675, 390, 150, 25);
		kValue.setVisible(false);
		contentPane.add(kValue);
		
		
		// Value of Q - Contraharmonic Mean
		JLabel qValueLabel = new JLabel("Value of Q");
		qValueLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		qValueLabel.setBounds(560, 390, 110, 25);
		qValueLabel.setVisible(false);
		contentPane.add(qValueLabel);
		
		qValue = new JSpinner();
		qValue.setModel(new SpinnerNumberModel(1.0, null, null, 1.0));
		qValue.setBounds(675, 390, 150, 25);
		qValue.setVisible(false);
		contentPane.add(qValue);
		
		
		// Value of d - Alpha-trimmed Mean
		JLabel dValueLabel = new JLabel("Value of d");
		dValueLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		dValueLabel.setBounds(560, 390, 110, 25);
		dValueLabel.setVisible(false);
		contentPane.add(dValueLabel);
		
		dValue = new JSpinner();
		dValue.setModel(new SpinnerNumberModel(0, 0, null, 2));
		dValue.setBounds(675, 390, 150, 25);
		dValue.setVisible(false);
		contentPane.add(dValue);
		
		
		// Variance - Gaussian Noise Filter
		JLabel varianceLabel = new JLabel("Variance");
		varianceLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		varianceLabel.setBounds(560, 350, 110, 25);
		varianceLabel.setVisible(false);
		contentPane.add(varianceLabel);
		
		varianceGaussian = new JSpinner();
		varianceGaussian.setModel(new SpinnerNumberModel(400, 0, null, 100));
		varianceGaussian.setBounds(675, 350, 150, 25);
		varianceGaussian.setVisible(false);
		contentPane.add(varianceGaussian);
		
		
		// Mean - Gaussian Noise Filter
		JLabel meanLabel = new JLabel("Mean");
		meanLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		meanLabel.setBounds(560, 390, 110, 25);
		meanLabel.setVisible(false);
		contentPane.add(meanLabel);
		
		mean = new JSpinner();
		mean.setModel(new SpinnerNumberModel(0, 0, null, 1));
		mean.setBounds(675, 390, 150, 25);
		mean.setVisible(false);
		contentPane.add(mean);
		
		
		// Noise Type - Salt & Pepper Filter
		JLabel noiseTypesLabel = new JLabel("Noise Type");
		noiseTypesLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		noiseTypesLabel.setBounds(560, 350, 110, 25);
		noiseTypesLabel.setVisible(false);
		contentPane.add(noiseTypesLabel);
		
		noiseTypes = new JComboBox(new String[]{"salt & pepper", "salt", "pepper"});
		noiseTypes.setFont(new Font("Tahoma", Font.PLAIN, 11));
		noiseTypes.setBounds(675, 350, 150, 25);
		noiseTypes.setVisible(false);
		contentPane.add(noiseTypes);

		// Density - Salt & Pepper Filter
		JLabel densityLabel = new JLabel("Density");
		densityLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		densityLabel.setBounds(560, 390, 110, 25);
		densityLabel.setVisible(false);
		contentPane.add(densityLabel);
		
		density = new JSpinner();
		density.setModel(new SpinnerNumberModel(0.05, 0.00, null, 0.01));
		density.setBounds(675, 390, 150, 25);
		density.setVisible(false);
		contentPane.add(density);
		
		
		// Variance - Speckle Noise Filter		
		varianceSpeckle = new JSpinner();
		varianceSpeckle.setModel(new SpinnerNumberModel(0.05, 0.00, null, 0.01));
		varianceSpeckle.setBounds(675, 350, 150, 25);
		varianceSpeckle.setVisible(false);
		contentPane.add(varianceSpeckle);
		
		
		JRadioButton clippingRadioButton = new JRadioButton("Clipping");
		clippingRadioButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		clippingRadioButton.setBounds(560, 390, 110, 25);
		clippingRadioButton.setSelected(true);
		clippingRadioButton.setVisible(false);
		contentPane.add(clippingRadioButton);
		
		JRadioButton scalingRadioButton = new JRadioButton("Scaling");
		scalingRadioButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scalingRadioButton.setBounds(715, 390, 110, 25);
		scalingRadioButton.setVisible(false);
		contentPane.add(scalingRadioButton);
		
		laplacianButtonGroup = new ButtonGroup();
		laplacianButtonGroup.add(clippingRadioButton);
		laplacianButtonGroup.add(scalingRadioButton);
		clippingRadioButton.setActionCommand("clipping");
		scalingRadioButton.setActionCommand("scaling");
		
		withErosion = new JCheckBox("Erode");
		withErosion.setBounds(560, 390, 110, 25);
		withErosion.setVisible(false);
		contentPane.add(withErosion);
		
		withRefinement = new JCheckBox("Refine");
		withRefinement.setBounds(715, 390, 110, 25);
		withRefinement.setVisible(false);
		contentPane.add(withRefinement);
		
		
		filteringOperations.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e) 
		    {	
		    	// String filteringOption = filteringOperations.getSelectedItem().toString();
		    	int filteringOption = filteringOperations.getSelectedIndex();
		    	
		    	standardDeviationLabel.setVisible(filteringOption == 2);
		    	standardDeviation.setVisible(filteringOption == 2);
		    	
		    	kValueLabel.setVisible(filteringOption == 6);
		    	kValue.setVisible(filteringOption == 6);
		    	
		    	qValueLabel.setVisible(filteringOption == 10);
		    	qValue.setVisible(filteringOption == 10);
		    	
		    	dValueLabel.setVisible(filteringOption == 14);
		    	dValue.setVisible(filteringOption == 14);
		    	
		    	varianceLabel.setVisible(filteringOption == 15 || filteringOption == 18);
		    	varianceGaussian.setVisible(filteringOption == 15);
		    	varianceSpeckle.setVisible(filteringOption == 18);
		    	
		    	meanLabel.setVisible(filteringOption == 15);
		    	mean.setVisible(filteringOption == 15);
		    	
		    	
		    	noiseTypesLabel.setVisible(filteringOption == 17);
		    	noiseTypes.setVisible(filteringOption == 17);	   
		    	
		    	densityLabel.setVisible(filteringOption == 17);
		    	density.setVisible(filteringOption == 17);	    	
		    	
		    	clippingRadioButton.setVisible(filteringOption == 4 ||filteringOption == 5);
	    		scalingRadioButton.setVisible(filteringOption == 4 ||filteringOption == 5);
	    		
	    		
	    		if(filteringOption > 0 && filteringOption < 7)
		    	{
	    			filterSizeLabel.setText("Kernel Radius");
		    	}
		    	else if(filteringOption >= 7 && filteringOption < 15)
		    	{
		    		filterSizeLabel.setText("Subimage Size");
		    	}
		    	else if(filteringOption >= 19)
		    	{
		    		filterSizeLabel.setText("Patch Radius");
		    	}
	    		
	    		filterSizeLabel.setVisible((filteringOption > 0 && filteringOption < 15) || filteringOption >= 19);
	    		filterKernelRadius.setVisible(filteringOption > 0 && filteringOption < 7);
	    		filterXLabel.setVisible(filteringOption >= 7 && filteringOption < 15);
	    		filterSubimageWidth.setVisible(filteringOption >= 7 && filteringOption < 15);
	    		filterSubimageHeight.setVisible(filteringOption >= 7 && filteringOption < 15);
	    		hazeRemovalPatchRadius.setVisible(filteringOption >= 19);
	    		
	    		withErosion.setVisible(filteringOption >= 20);
	    		withRefinement.setVisible(filteringOption >= 20);
		    }
		});
		

		// Removed Bit Planes
		JLabel BitPlanesRemovalLabel = new JLabel("Bit-planes Removal");
		BitPlanesRemovalLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		BitPlanesRemovalLabel.setBounds(560, 450, 110, 25);
		contentPane.add(BitPlanesRemovalLabel);
		
		JCheckBox bitPlane1 = new JCheckBox("1");
		bitPlanes[0] = bitPlane1;
		bitPlane1.setBounds(675, 450, 40, 25);
		contentPane.add(bitPlane1);
		
		JCheckBox bitPlane2 = new JCheckBox("2");
		bitPlanes[1] = bitPlane2;
		bitPlane2.setBounds(715, 450, 40, 25);
		contentPane.add(bitPlane2);
		
		JCheckBox bitPlane3 = new JCheckBox("3");
		bitPlanes[2] = bitPlane3;
		bitPlane3.setBounds(755, 450, 40, 25);
		contentPane.add(bitPlane3);
		
		JCheckBox bitPlane4 = new JCheckBox("4");
		bitPlanes[3] = bitPlane4;
		bitPlane4.setBounds(795, 450, 40, 25);
		contentPane.add(bitPlane4);
		
		JCheckBox bitPlane5 = new JCheckBox("5");
		bitPlanes[4] = bitPlane5;
		bitPlane5.setBounds(675, 480, 40, 25);
		contentPane.add(bitPlane5);
		
		JCheckBox bitPlane6 = new JCheckBox("6");
		bitPlanes[5] = bitPlane6;
		bitPlane6.setBounds(715, 480, 40, 25);
		contentPane.add(bitPlane6);
		
		JCheckBox bitPlane7 = new JCheckBox("7");
		bitPlanes[6] = bitPlane7;
		bitPlane7.setBounds(755, 480, 40, 25);
		contentPane.add(bitPlane7);
		
		JCheckBox bitPlane8 = new JCheckBox("8");
		bitPlanes[7] = bitPlane8;
		bitPlane8.setBounds(795, 480, 40, 25);
		contentPane.add(bitPlane8);
	

		// Separators
		JSeparator separator1 = new JSeparator();
		separator1.setBounds(560, 130, 265, 1);
		contentPane.add(separator1);
		
		JSeparator separator2 = new JSeparator();
		separator2.setBounds(560, 190, 265, 1);
		contentPane.add(separator2);
		
		JSeparator separator3 = new JSeparator();
		separator3.setBounds(560, 290, 265, 1);
		contentPane.add(separator3);
	
		JSeparator separator4 = new JSeparator();
		separator4.setBounds(560, 430, 265, 1);
		contentPane.add(separator4);

		
		// Buttons
		JButton processButton = new JButton("PROCESS");
		processButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		processButton.setBounds(560, 532, 265, 30);
		contentPane.add(processButton);
		
		JButton browseButton = new JButton("BROWSE");
		browseButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		browseButton.setBounds(25, 580, 200, 30);
		contentPane.add(browseButton);
		
		
		JButton calculateRMSEButton = new JButton("CALCULATE RMSE");
		calculateRMSEButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		calculateRMSEButton.setBounds(850, 580, 160, 30);
		contentPane.add(calculateRMSEButton);
		
		JButton setAsOriginalImageButton = new JButton("SET AS ORIGINAL IMAGE");
		setAsOriginalImageButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		setAsOriginalImageButton.setBounds(1026, 580, 160, 30);
		contentPane.add(setAsOriginalImageButton);		
		
		JButton showHistogramButton = new JButton("SHOW HISTOGRAM");
		showHistogramButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		showHistogramButton.setBounds(1202, 580, 160, 30);
		contentPane.add(showHistogramButton);
		

		JLabel compressionLabel = new JLabel("Compression");
		compressionLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		compressionLabel.setBounds(295, 585, 70, 25);
		contentPane.add(compressionLabel);
		
		compressionMethods = new JComboBox(new String[]{"-", "RLC (Grayscale values)", "RLC (Bit Planes)", "Huffman"});
		compressionMethods.setFont(new Font("Tahoma", Font.PLAIN, 11));
		compressionMethods.setBounds(367, 585, 170, 25);
		contentPane.add(compressionMethods);
		
		JButton compressButton = new JButton("COMPRESS");
		compressButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		compressButton.setBounds(560, 580, 265, 30);
		contentPane.add(compressButton);
		
		JLabel instruction1 = new JLabel("(Double click on image to resize it to fit the view)");
		instruction1.setHorizontalAlignment(SwingConstants.CENTER);
		instruction1.setFont(new Font("Tahoma", Font.PLAIN, 10));
		instruction1.setBounds(25, 25, 512, 25);
		contentPane.add(instruction1);
		
		JLabel instruction2 = new JLabel("(Click and hold on the image to see the changes after being processed)");
		instruction2.setHorizontalAlignment(SwingConstants.CENTER);
		instruction2.setFont(new Font("Tahoma", Font.PLAIN, 10));
		instruction2.setBounds(850, 25, 512, 25);
		contentPane.add(instruction2);

		
		processButton.addMouseListener(new MouseAdapter() 
		{
            @Override
            public void mouseClicked(MouseEvent e) 
            {
            	if(originalImg != null)
            	{
            		try 
                	{	
                 	    if ((int)filterSubimageWidth.getValue() % 2 == 0)
                 	    {
                 	    	throw new NumberFormatException();
                 	    }
                 	} 
                 	catch (NumberFormatException ex) 
                 	{
                 		JOptionPane.showMessageDialog(new JFrame(), "Invalid value for 'Filter Kernel Width'", "Error", JOptionPane.ERROR_MESSAGE);
                 		return;
                 	}
            		
            		
            		try 
                	{	
            			if ((int)filterSubimageHeight.getValue() % 2 == 0)
                 	    {
                 	    	throw new NumberFormatException();
                 	    }
                 	} 
                 	catch (NumberFormatException ex) 
                 	{
                 		JOptionPane.showMessageDialog(new JFrame(), "Invalid value for 'Filter Kernel Height'", "Error", JOptionPane.ERROR_MESSAGE);
                 		return;
                 	}
            		
            		
            		try 
                	{	
            			if ((int)dValue.getValue() % 2 == 1)
                 	    {
                 	    	throw new NumberFormatException();
                 	    }
                 	} 
                 	catch (NumberFormatException ex) 
                 	{
                 		JOptionPane.showMessageDialog(new JFrame(), "Invalid value for 'Value of d'", "Error", JOptionPane.ERROR_MESSAGE);
                 		return;
                 	}
            		
            		
            		removedBitPlanes = new ArrayList<Integer>();
            		for(int i = 0; i < bitPlanes.length; i++)
            		{
            			if(bitPlanes[i].isSelected()) 	removedBitPlanes.add(i + 1);
            		}
            		
           		   	
                	// Perform image procession based on the input data
                	processImage();
                	
                	// Show the output image
                	processedImage.setIcon(new ImageIcon(processedImg));
            	}
            	else
            	{
            		JOptionPane.showMessageDialog(new JFrame(), "Please select an original image", "Error", JOptionPane.ERROR_MESSAGE);
            	}
            }
        });		
		
		
		compressButton.addMouseListener(new MouseAdapter() 
		{
            @Override
            public void mouseClicked(MouseEvent e) 
            {
            	if(originalImg != null)
            	{
            		compressImage();
            	}
            	else
            	{
            		JOptionPane.showMessageDialog(new JFrame(), "Please select an original image", "Error", JOptionPane.ERROR_MESSAGE);
            	}
            }
        });		
		
		
		browseButton.addMouseListener(new MouseAdapter() 
		{
            @Override
            public void mouseClicked(MouseEvent e) 
            {
            	JFileChooser fileChooser = new JFileChooser();
    			int option = fileChooser.showDialog(new JFrame(), "Choose image file");
    			if (option == JFileChooser.APPROVE_OPTION)
    			{
    				try 
    				{
    					originalImg = ImageIO.read(fileChooser.getSelectedFile());
    				} 
    				catch (IOException ex) 
    				{
    				    ex.printStackTrace();
    				}
    				
    				setOriginalImage(originalImg);
    			}
    			
            };
        });
		
		
		calculateRMSEButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				Processor.calculateRMSE(originalImg, processedImg);
			}
		});
		
		
		setAsOriginalImageButton.addMouseListener(new MouseAdapter() 
		{
            @Override
            public void mouseClicked(MouseEvent e) 
            {
            	originalImg = (BufferedImage)((ImageIcon)processedImage.getIcon()).getImage();
            	setOriginalImage(originalImg);
            };
        });			
	
	
		showHistogramButton.addMouseListener(new MouseAdapter() 
		{
            @Override
            public void mouseClicked(MouseEvent e) 
            {
            	Processor.generateHistogram(processedImg);
            };
		});
	}
	
	
	/**
	 * Process the input image
	 */
	private void processImage()
	{
		processedImg = originalImg;	
		
		// Resize image based on chosen scaling algorithm
		Interpolator interpolators[] = { new NearestNeighborInterpolator(), new LinearInterpolator(Interpolator.Direction.X), new LinearInterpolator(Interpolator.Direction.Y), new BilinearInterpolator() };
		if(scalingAlgorithms.getSelectedIndex() != 0)
		{
			processedImg = interpolators[scalingAlgorithms.getSelectedIndex() - 1].process(processedImg, (int)imageWidth.getValue(), (int)imageHeight.getValue());
		}
		
		
		// Convert image to pixelSize-bit color image
		processedImg = new PixelSizeConverter().process(processedImg, (int)pixelSize.getValue());	
		
		
		// Perform histogram equalization
		String histogramEqualizationOption = histogramEqualizationOptions.getSelectedItem().toString();
		if(!histogramEqualizationOption.equals("-"))
		{
			if(histogramEqualizationOption.equals("Global"))
			{
				processedImg = new HistogramEqualizator().process(processedImg);
			}
			else if(histogramEqualizationOption.equals("Local"))
			{
				processedImg = new HistogramEqualizator().process(processedImg, (int)heKernelRadius.getValue());
			} 
		}
		
		
		// Remove chosen bit-planes
		processedImg = new BitPlanesRemover().process(processedImg, removedBitPlanes);
		
		
		// Filter image based on chosen filtering operation
		if(filteringOperations.getSelectedIndex() != 0)
		{
			if(filteringOperations.getSelectedIndex() < 7)
			{
				SpatialFilter filters[] = { 
												new AveragingFilter(),
												new GaussianFilter((double)standardDeviation.getValue()), 
												new MedianFilter(), 
												new LaplacianFilter(laplacianButtonGroup.getSelection().getActionCommand()), 
												new ImageSharpeningFilter(laplacianButtonGroup.getSelection().getActionCommand()),
												new HighBoostingFilter((double)kValue.getValue())
										   };
				
				processedImg = filters[filteringOperations.getSelectedIndex() - 1].process(processedImg, (int)filterKernelRadius.getValue());
			}
			else if(filteringOperations.getSelectedIndex() < 15)
			{
				DenoisingFilter filters[] = {
												new ArithmeticMeanFilter(),
												new GeometricMeanFilter(),
												new HarmonicMeanFilter(),
												new ContraharmonicMeanFilter((double)qValue.getValue()),
												new MaxFilter(),
												new MinFilter(),
												new MidpointFilter(),
												new AlphaTrimmedMeanFilter((int)dValue.getValue()),
											};
				
				processedImg = filters[filteringOperations.getSelectedIndex() - 7].process(processedImg, (int)filterSubimageWidth.getValue(), (int)filterSubimageHeight.getValue());
			}
			else if(filteringOperations.getSelectedIndex() < 19)
			{
				ImnoisingFilter filters[] = {
												new GaussianNoiseFilter((int)mean.getValue(), (int)varianceGaussian.getValue()),
												new PoissonNoiseFilter(),
												new SaltAndPepperNoiseFilter(noiseTypes.getSelectedItem().toString(), (double)density.getValue()),
												new SpeckleNoiseFilter((double)varianceSpeckle.getValue()),
											};
				
				processedImg = filters[filteringOperations.getSelectedIndex() - 15].process(processedImg);
			}
			else
			{
				HazeRemover filters[] = {		
											new DarkChannel(),
											new TransmissionMap(withErosion.isSelected(), withRefinement.isSelected()),
											new HazeRemovalFilter(withErosion.isSelected(), withRefinement.isSelected()),
										 };
				
				processedImg = filters[filteringOperations.getSelectedIndex() - 19].process(processedImg, (int)hazeRemovalPatchRadius.getValue());
			}
		}
	}
	
	
	/**
	 * Compress the input image
	 */
	private void compressImage()
	{
		// Filter image based on chosen filtering operation
		if(compressionMethods.getSelectedIndex() != 0)
		{
			Compressor compressors[] = {
											new RLCGrayScalesCompressor(),
											new RLCBitPlanesCompressor(),
											new HuffmanCodingCompressor(),
										};
			
			compressors[compressionMethods.getSelectedIndex() - 1].compress(originalImg);
		}
	}
	
	/**
	 * Show the chosen image in the original image's position
	 * Set the chosen image data (pixel size, width, and height) to the associated fields
	 * @param image the chosen image
	 */
	private void setOriginalImage(BufferedImage image)
	{
		originalImage.setIcon(new ImageIcon(image));

    	pixelSize.setValue(image.getColorModel().getPixelSize());
    	imageWidth.setValue(image.getWidth());
    	imageHeight.setValue(image.getHeight());
	}
}
