/*
 * Mauricio Renon
 * ICS432
 * 
 * DataParallelWeirdFilter.java
 * 
 * applies a weird filter to an image
 * that many threads can acess
 * 
 *
 */
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.ArrayList;



/**
 * DataParallelWierdFilter class
 * Takes in a given image
 * and applies a weird filter to the image
 * @author mauriciofloydianrenon
 *
 */
public class DataParallelWeirdFilter implements BufferedImageOp {


	int threadCount;
	
	public DataParallelWeirdFilter(int s1) {
		this.threadCount = s1;
	}
	 /*
	 * processPixel takes a given pixel 
	 * finds all its legal neighbors and applies the filter
	 * 
	 * @param image the Buffered image to get the pixel
	 * @param x the x value of the pixel
	 * @param y the y value of the pixel
	 * 
	 */
	private int processPixel(BufferedImage image, int x, int y) {
		ArrayList<Integer> xVal = new ArrayList<Integer>();
		ArrayList<Integer> yVal = new ArrayList<Integer>();
		int rgb = image.getRGB(x,y);
		byte[] bytes = RGB.intToBytes(rgb);
		
		bytes[0] = 0;
		bytes[1] = 0;
		bytes[2] = 0;
		
		
		for (int i = -1; i < 2; i++)
		{
			for (int j = -1; j < 2; j++){
				if (i != 0 || j != 0)
				{
					int rx = x + i;
					int ry = y + j;
					
					if (rx >= 0 && rx < image.getWidth() && ry >= 0 && ry < image.getHeight())
					{
						xVal.add(rx);
						yVal.add(ry);
					}
				}
			}
		}
		
		
		
		for (int k = 0; k < xVal.size(); k++)
		{
			int pixel = image.getRGB(xVal.get(k), yVal.get(k));
			byte[] newval = RGB.intToBytes(pixel);
			bytes[0] += Math.max(Math.exp(newval[0]), 20) + 10*Math.cos(newval[0]);
			bytes[1] += Math.min(Math.exp(newval[1]), 50);
			bytes[2] += Math.min(Math.exp(newval[2]), 20);
			
		}
		
		
		bytes[0] /= xVal.size();
		bytes[1] /= xVal.size();
		bytes[2] /= xVal.size();

		xVal.removeAll(xVal);
		yVal.removeAll(yVal);
		
		return RGB.bytesToInt(bytes);
	}


	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth()/threadCount;
		int height = src.getHeight(); 	
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int sizeHeight = (int) Math.floor(height / threadCount);
		int extraPixel = sizeHeight % threadCount;
		
		int minx = 0;
		int miny = 0;
		
		int max_x = width;
		int max_y = sizeHeight;
		

		for (int i = 0; i <= threadCount-1; i++){
			processThread t = new processThread(src, dest, minx, miny,max_x,max_y);
			threads.add(t);
			threads.get(i).start();
			miny = max_y;
			max_y = miny + sizeHeight;

		}
		//processThread t = new processThread(src, dest, minx, miny, max_x, height);
		//t.start();
		for(int i = 0; i <= threadCount-1; i++){
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dest;
	}
	
	@Override
	public Rectangle2D getBounds2D(BufferedImage src) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage src,
			ColorModel destCM) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 /**
	 * processThread class
	 * creates threads, based on a command-line
	 * argument that the user specified to apply the
	 * filter to the image
	 * @author mauriciofloydianrenon
	 *
	 */
	public class processThread extends Thread
	{
		int minx, miny; 
		int upperx, uppery;
		BufferedImage src;
		BufferedImage dest;
		
		 /*
		 * processThread creates threads and processes images
		 * by sharing the same image
		 * 
		 * @param src the original image
		 * @param dest the filtered image
		 * @param lowx the low x value
		 * @param lowy the low y value
		 * @param upx the upper x value
		 * @param upy the upper y value
		 * 
		 */
		public processThread(BufferedImage src, BufferedImage dest, int lowx, int lowy, int upx, int upy)
		{
			this.minx = lowx;
			this.miny = lowy;
			this.upperx = upx;
			this.uppery = upy;
			this.src = src;
			this.dest = dest;
		}
		
	 	/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
			for (int i = minx; i < upperx; i++) {
				for (int j = miny; j < uppery; j++) {	
					dest.setRGB(i, j, processPixel(src, i, j));
				}
			}
			
			
		}
		
	}
}


