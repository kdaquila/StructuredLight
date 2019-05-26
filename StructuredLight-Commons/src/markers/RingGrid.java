package markers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import core.PNG;

public class RingGrid {
    
    public int nRows;
    public int nCols;
    
    RingGrid(int nRows, int nCols) {
        this.nRows = nRows;
        this.nCols = nCols;
    }
    
    public void draw(String savePath) {
        int borderWidth = 40;
        int ringOuterRadius = 120;
        int ringInnerRadius = 80;
        int ringMarginRadius = 200;
        draw(borderWidth, ringOuterRadius, ringInnerRadius, ringMarginRadius, savePath);
    }
    
    public void draw(int borderWidth, int ringOuterRadius, int ringInnerRadius, int ringMarginRadius, String savePath) {
        // compute page size
        int pageWidth = nCols*2*ringMarginRadius + 2*borderWidth;
        int pageHeight = nRows*2*ringMarginRadius + 2*borderWidth;

        // create image
        BufferedImage img = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
        
        // create graphics objects
        Graphics2D g = img.createGraphics();
        Graphics2D g2 = (Graphics2D)g;
        
        // set rendering hints
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        
        // Fill with White
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        
         // Draw boundary
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRect(0 + borderWidth/2, 0 + borderWidth/2, pageWidth - borderWidth, pageHeight - borderWidth);
        
        // Draw Ring's Black Disc
        int outerOffset = borderWidth + (ringMarginRadius - ringOuterRadius);
        for (int x = outerOffset; x < pageWidth - outerOffset; x+= 2*ringMarginRadius ){
            for (int y = outerOffset; y < pageHeight - outerOffset; y+= 2*ringMarginRadius){
                g2.setPaint(Color.BLACK);                             
                g2.fillOval(x, y, 2*ringOuterRadius, 2*ringOuterRadius);
            }
        }
        
        // Draw Ring's White Disc
        int innerOffset = borderWidth + (ringMarginRadius - ringInnerRadius);
        for (int x = innerOffset; x < pageWidth - innerOffset; x+= 2*ringMarginRadius ){
            for (int y = innerOffset; y < pageHeight - innerOffset; y+= 2*ringMarginRadius){
                g2.setPaint(Color.WHITE);                             
                g2.fillOval(x, y, 2*ringInnerRadius, 2*ringInnerRadius);
            }
        }
        
        // save
        int DPI = 300;
        PNG png = new PNG();        
        png.setDPI(DPI);
        png.save(img, savePath);
    }
    
    public void find() {
        // TODO implement RingGrid find() method
    }
    
    
}
