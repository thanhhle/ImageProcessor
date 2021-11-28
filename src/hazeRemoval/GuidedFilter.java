package hazeRemoval;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class GuidedFilter 
{	
	static 
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public double[][][] process(double[][][] filteringRGBs, double[][][] guidanceRGBs, int r, double eps)
	{
	    Mat p = toMat(filteringRGBs);
		Mat I = toMat(guidanceRGBs);
		
		// mean_I = mean(I)
		Mat mean_I = new Mat();
		meanFilter(I, mean_I, r);
		
		// mean_p = mean(p)
		Mat mean_p = new Mat();
		meanFilter(p, mean_p, r);
				
		// corr_I = mean(I. * I, r)
		Mat corr_I = new Mat();
		meanFilter(I.mul(I), corr_I, r);
		
		// corr_Ip = mean(I. * p, r)
		Mat corr_Ip = new Mat();
		meanFilter(I.mul(p), corr_Ip, r);
		
		// var_I = corr_I - mean_I. * mean_I
		Mat var_I = new Mat();
		Core.subtract(corr_I, mean_I.mul(mean_I), var_I);
		
		// cov_Ip = corr_Ip - mean_I. * mean_p
		Mat cov_Ip = new Mat();
		Core.subtract(corr_Ip, mean_I.mul(mean_p), cov_Ip);
		
		// a = cov_Ip ./ (var_I + eps)
		Mat a = new Mat();
		Core.add(var_I, new Scalar(eps), a);
		Core.divide(cov_Ip, a, a);
		
		// b = mean_p - a .* mean_I;
		Mat b = new Mat();
		Core.subtract(mean_p, a.mul(mean_I), b);
		
		// mean_a = mean(a)
		Mat mean_a = new Mat();
		meanFilter(a, mean_a, r);
		
		// mean_b = mean(b)
		Mat mean_b = new Mat();
		meanFilter(b, mean_b, r);
		
		// q = mean_a .* I + mean_b
		Mat q = new Mat();
		Core.add(mean_a.mul(I), mean_b, q);
		
		double[][][] transmissionRefine = toArray(q);
		return transmissionRefine;
	}
	
	/**
	 * Apply Mean filter on a source image with given mask radius and store the processed image to the provided destination image
	 * @param src source image
	 * @param dst destination image
	 * @param r mask radius
	 */
	private void meanFilter(Mat src, Mat dst, int r)
	{
		// Apply the Box filter
		Imgproc.boxFilter(src, dst, -1, new Size(r, r));
		
		// Construct an identity matrix
		Mat N = new Mat();
		Imgproc.boxFilter(Mat.ones(src.rows(), src.cols(), src.type()), N, -1, new Size(r, r));
		
		// Normalize the filtered image with the constructed identity matrix
		Core.divide(dst, N, dst);
	}
	
	
	/**
	 * Convert the RGB array to Mat object
	 * @param rgbs input RGB array
	 * @return RGB mat
	 */
	private static Mat toMat(double[][][] rgbs)
	{
		Mat mat = new Mat(rgbs.length, rgbs[0].length, CvType.CV_64F);
		
		for(int x = 0; x < rgbs.length; x++) 
		{
			for(int y = 0; y < rgbs[0].length; y++) 
			{
				mat.put(x, y, rgbs[x][y]);
			}
		}
		
		Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE);
		Core.flip(mat, mat, 1);
		
		return mat;
	}
	
	
	/**
	 * Convert a RGB mat to RGB array
	 * @param mat input RGB mat
	 * @return RGB array
	 */
	private static double[][][] toArray(Mat mat)
	{
		return HazeRemover.getRGBs(toBufferedImage(mat));
	}
		
	
	/**
	 * Convert a RGB mat to BufferedImage object
	 * @param mat input RGB mat
	 * @return BufferedImage object
	 */
	private static BufferedImage toBufferedImage(Mat mat)
	{
	  MatOfByte matOfByte = new MatOfByte();
	  Imgcodecs.imencode(".bmp", mat, matOfByte);
	      
	  byte[] data = matOfByte.toArray();
	  
	  BufferedImage bufferedImage = null;
	  try 
	  {
		  bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
	  } 
	  catch (IOException e) 
	  {
		  e.printStackTrace();
	  }
	  
	  return bufferedImage;
	}
}
