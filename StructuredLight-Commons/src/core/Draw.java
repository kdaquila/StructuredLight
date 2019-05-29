package core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

public class Draw {
    
    
    public static BufferedImage drawCircles(BufferedImage img, List<List<Integer>> centers, int radius, float lineWidth, Integer colorHex, boolean isFill) 
    {
        // Create an new rgb image
        int width = img.getWidth();
        int height = img.getHeight();
        int rgbType = BufferedImage.TYPE_INT_RGB;
        BufferedImage rgbImage = new BufferedImage(width, height, rgbType);        
        Graphics2D g = rgbImage.createGraphics();
        
        // Draw the existing image
        g.drawImage(img, 0, 0, null);                
        
        // Define colors
        List<Integer> colors = new ArrayList<>();
        int color_index = 0;
        if (colorHex == null) {        
            colors.add(0xFF00FF);
            colors.add(0xFFFF00);
            colors.add(0x00FFFF);
        } else {
            colors.add(colorHex);
        }
        
        // Draw the circles
        for (List<Integer> center: centers)
        {
            int centerX = center.get(0);
            int centerY = center.get(1);
            int x = centerX - radius;
            int y = centerY - radius;
            int w = radius*2;
            int h = radius*2;
            g.setColor(new Color(colors.get(color_index)));
            g.setStroke(new BasicStroke(lineWidth));
            g.drawOval(x, y, w, h);
            
            if (isFill) {
                g.fillOval(x, y, w, h);
            }
            
            // change to next color
            color_index = (color_index + 1)%colors.size();
        }
        
        return rgbImage;
    }
    
    public static BufferedImage drawContours(BufferedImage img, List<List<List<Integer>>> contours)
    {
        // Create an new rgb image
        int width = img.getWidth();
        int height = img.getHeight();
        int rgbType = BufferedImage.TYPE_INT_RGB;
        BufferedImage rgbImage = new BufferedImage(width, height, rgbType);
        Graphics2D g = rgbImage.createGraphics();
        
        // Draw the existing image
        g.drawImage(img, 0, 0, null);
        
        // Get data buufer
        int[] rgbBuffer = ((DataBufferInt)rgbImage.getRaster().getDataBuffer()).getData();
        
        // Define colors
        List<Integer> colors = new ArrayList<>();
        colors.add(0xFF00FF);
        colors.add(0xFFFF00);
        colors.add(0x00FFFF);
        int color_index = 0;
        
        // Write colors to buffer pixels
        for (List<List<Integer>> contour: contours)
        {
            for (List<Integer> point: contour)
            {
                int x = point.get(0);
                int y = point.get(1); 
                int index = y*width + x;
                rgbBuffer[index] = colors.get(color_index);                
            }
            color_index = (color_index + 1)%colors.size();
        }
        
        return rgbImage;
    } 

}
