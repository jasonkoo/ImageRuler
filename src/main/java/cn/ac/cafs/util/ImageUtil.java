package cn.ac.cafs.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;


public class ImageUtil {
	public static BufferedImage resize(BufferedImage original, int widthLimit, int heightLimit) {
		int origWidth = original.getWidth();
		int origHeight = original.getHeight();
		double widthFactor = (double)widthLimit/(double)origWidth;
		double heightFactor = (double)heightLimit/(double)origHeight;
		double scaleFactor = widthFactor <= heightFactor ? widthFactor : heightFactor;
		
		int newWidth = new Double(origWidth * scaleFactor).intValue();
		int newHeight = new Double(origHeight * scaleFactor).intValue();
		
		BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.getWidth(), original.getHeight(), null);
		g.dispose();
		return resized;
	}
	
	public static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	

}
