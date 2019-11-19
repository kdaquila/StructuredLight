package calibrationpattern.rings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ModelRings {
    
    public int nRows;
    public int nCols;
    public int borderWidth;    
    public int shapeRadius;
    public int pageWidth;
    public int pageHeight;
    public BufferedImage img;
    private Graphics2D g;
    
    public ModelRings(int nRows, int nCols, int borderWidth, int shapeRadius) {
        this.nRows = nRows;
        this.nCols = nCols;
        this.borderWidth = borderWidth;
        this.shapeRadius = shapeRadius;
        pageWidth = nCols*2*shapeRadius + 2*borderWidth;
        pageHeight = nRows*2*shapeRadius + 2*borderWidth;
        img = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
        g = img.createGraphics();
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
                                              RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHints(hints);
    }
    
    public void drawFill(Color c) {
        g.setPaint(c);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
    }
    
    public void drawBorder(Color c) {
        g.setPaint(c);
        g.setStroke(new BasicStroke(borderWidth));
        int offset = borderWidth/2;
        int width = pageWidth - borderWidth;
        int height = pageHeight - borderWidth;
        g.drawRect(offset, offset, width, height);
    }
    
    public void drawRings(Color c, int ringOuterRadius, int ringInnerRadius) {
        // Draw Ring's Black Disc
        int blackDiscStart = borderWidth + (shapeRadius - ringOuterRadius);
        int blackDiscStopX = pageWidth - blackDiscStart;
        int blackDiscStopY = pageHeight - blackDiscStart;
        int blackDiscStep = 2*shapeRadius;
        int blackDiscSize = 2*ringOuterRadius;
        for (int x = blackDiscStart; x < blackDiscStopX; x+= blackDiscStep) {
            for (int y = blackDiscStart; y < blackDiscStopY; y+= blackDiscStep) {
                g.setPaint(Color.BLACK);                             
                g.fillOval(x, y, blackDiscSize, blackDiscSize);
            }
        }        
        
        // Draw Ring's White Disc        
        int whiteDiscStart = borderWidth + (shapeRadius - ringInnerRadius);
        int whiteDiscStopX = pageWidth - whiteDiscStart;
        int whiteDiscStopY = pageHeight - whiteDiscStart;
        int whiteDiscStep = 2*shapeRadius;
        int whiteDiscSize = 2*ringInnerRadius;
        for (int x = whiteDiscStart; x < whiteDiscStopX; x+= whiteDiscStep) {
            for (int y = whiteDiscStart; y < whiteDiscStopY; y+= whiteDiscStep) {
                g.setPaint(Color.WHITE);                             
                g.fillOval(x, y, whiteDiscSize, whiteDiscSize);
            }
        }  
    }
}
