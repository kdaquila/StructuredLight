package calibrationpattern.gradientdiscs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ModelDiscs {

    public int nRows;
    public int nCols;
    public int borderWidth;    
    public int halfPitch;
    public int pageWidth;
    public int pageHeight;
    public BufferedImage img;
    private Graphics2D g;
    
    public ModelDiscs(int nRows, int nCols, int borderWidth, int halfPitch) {
        this.nRows = nRows;
        this.nCols = nCols;
        this.borderWidth = borderWidth;
        this.halfPitch = halfPitch;
        pageWidth = nCols*2*halfPitch + 2*borderWidth;
        pageHeight = nRows*2*halfPitch + 2*borderWidth;
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
    
    public void drawDiscs(int discRadius, int gradRadius) {

        // Draw Gradient Disc
        int discStart = borderWidth;
        int discStopX = pageWidth - borderWidth;
        int discStopY = pageHeight - borderWidth;
        int discStep = 2*halfPitch;
        double rMax = gradRadius;
        double IMax = 255.0;
        for (int x0 = discStart + discStep/2; x0 < discStopX; x0+= discStep) {
            for (int y0 = discStart + discStep/2; y0 < discStopY; y0+= discStep) {                
                
                g.setPaint(Color.BLACK);                             
                g.fillOval(x0 - discRadius, y0 - discRadius, 2*discRadius, 2*discRadius);
                
                for (int i = x0-gradRadius; i < x0 + gradRadius; i++) {
                    for (int j = y0-gradRadius; j < y0 + gradRadius; j++) {
                        double r = Math.sqrt(Math.pow(i - x0, 2) + Math.pow(j - y0, 2));
                        int I = 0;
                        if (r <= rMax) {
                            I = (int)(IMax*(1-(r/rMax)));
                        }                                                
                        g.setPaint(new Color(I, I, I)); 
                        g.fillRect(i, j, 1, 1);
                    }
                }                
            }
        }    
        

    }
}
