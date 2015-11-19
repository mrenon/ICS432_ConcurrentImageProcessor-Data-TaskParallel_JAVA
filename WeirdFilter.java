/*
 * Mauricio Renon
 * ICS432
 * 
 * WeirdFilter.java
 * 
 * applies a weird filter to an image
 * 
 *
 */

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * WierdFilter class
 * Takes in a given image
 * and applies a weird filter to the image
 * @author mauriciofloydianrenon
 *
 */
public class WeirdFilter implements BufferedImageOp{
	ArrayList<Integer> width = new ArrayList<Integer>();
	ArrayList<Integer> height= new ArrayList<Integer>();	

	public WeirdFilter(){
		
	}
	

	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage arg0,
			ColorModel arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage filter(BufferedImage arg0, BufferedImage arg1) {
		for (int i = 0; i < arg0.getWidth(); i++) {
			for (int j = 0; j < arg0.getHeight(); j++) {
				arg1.setRGB(i, j, processPixel(arg0, i, j));
			}
		}
		return null;
	}

	@Override
	public Rectangle2D getBounds2D(BufferedImage arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point2D getPoint2D(Point2D arg0, Point2D arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		// TODO Auto-generated method stub
		return null;
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
		
		int rgb = image.getRGB(x, y); // get the RGB value
        byte[] bytes = RGB.intToBytes(rgb); // split rgb value to 3 bytes
        
		
        bytes[0] = 0; // R 
        bytes[1] = 0; // G
        bytes[2] = 0; // B
 
        
        for (int i = -1; i < 2; i++) //getting the valid neighbors
        {
            for (int j = -1; j < 2; j++)
            {
            	if(i != 0 || j != 0){
            		int rx = x + i;
            		int ry = y + j;
            		if(rx >= 0 && rx < image.getWidth() &&
            			ry >= 0 && ry < image.getHeight()){
            			width.add(rx);
            			height.add(ry);
            		}	
            	}
            }
        }
        
        //System.out.println("Pixel: "+ x +","+ y);
       
         
        for (int k = 0; k < width.size(); k++)
        {
        	//System.out.println("   neighbor: "+width.get(k)+","+height.get(k));
        	int pixel = image.getRGB(width.get(k), height.get(k));
        	byte[] neighbor = RGB.intToBytes(pixel);
        	
            bytes[0] += Math.max(Math.exp(neighbor[0]), 20) + 10*Math.cos(neighbor[0]);
            bytes[1] += Math.min(Math.exp(neighbor[1]), 50);
            bytes[2] += Math.min(Math.exp(neighbor[2]), 20);
             
        }
         
        bytes[0] /= width.size();
        bytes[1] /= width.size();
        bytes[2] /= width.size();
 
        width.removeAll(width);
        height.removeAll(height);
        return RGB.bytesToInt(bytes); // converts RGB bytes into a pixel
    }
	
	
	

}
