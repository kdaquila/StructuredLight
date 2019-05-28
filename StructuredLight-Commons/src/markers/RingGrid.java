package markers;

import core.Contours;
import core.ImageUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import core.PNG;
import core.TXT;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    
    
    public void draw(int borderWidth, int ringOuterRadius, int ringInnerRadius, 
                     int ringMarginRadius, String savePath) {
        
        // Compute page size
        int pageWidth = nCols*2*ringMarginRadius + 2*borderWidth;
        int pageHeight = nRows*2*ringMarginRadius + 2*borderWidth;

        // Create image and graphics
        BufferedImage img = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        
        // Set rendering hints
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
                                               RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        
        // Fill with White
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        
         // Draw border
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(borderWidth));
        int borderStart = borderWidth/2;
        int borderAreaWidth = pageWidth - borderWidth;
        int borderAreaHeight = pageHeight - borderWidth;
        g2.drawRect(borderStart, borderStart, borderAreaWidth, borderAreaHeight);
        
        // Draw Ring's Black Disc
        int blackDiscStart = borderWidth + (ringMarginRadius - ringOuterRadius);
        int blackDiscStopX = pageWidth - blackDiscStart;
        int blackDiscStopY = pageHeight - blackDiscStart;
        int blackDiscStep = 2*ringMarginRadius;
        int blackDiscSize = 2*ringOuterRadius;
        for (int x = blackDiscStart; x < blackDiscStopX; x+= blackDiscStep) {
            for (int y = blackDiscStart; y < blackDiscStopY; y+= blackDiscStep) {
                g2.setPaint(Color.BLACK);                             
                g2.fillOval(x, y, blackDiscSize, blackDiscSize);
            }
        }        
        
        // Draw Ring's White Disc        
        int whiteDiscStart = borderWidth + (ringMarginRadius - ringInnerRadius);
        int whiteDiscStopX = pageWidth - whiteDiscStart;
        int whiteDiscStopY = pageHeight - whiteDiscStart;
        int whiteDiscStep = 2*ringMarginRadius;
        int whiteDiscSize = 2*ringInnerRadius;
        for (int x = whiteDiscStart; x < whiteDiscStopX; x+= whiteDiscStep) {
            for (int y = whiteDiscStart; y < whiteDiscStopY; y+= whiteDiscStep) {
                g2.setPaint(Color.WHITE);                             
                g2.fillOval(x, y, whiteDiscSize, whiteDiscSize);
            }
        }       
        
        // save
        int DPI = 300;
        PNG png = new PNG();        
        png.setDPI(DPI);
        png.save(img, savePath);
    }
        
    public void find(String rgbImgPath, String saveDataPath) {

        // Load the rgb image
        System.out.println("Loading the rgb image: Please wait ...");
        BufferedImage rgbImage = ImageUtil.load(rgbImgPath);
        
        // Convert RGB to gray
        System.out.println("Converting to grayscale: Please wait ...");
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImage);
                       
        // Adaptive threshold to black and white
        System.out.println("Adaptive thresholding to black and white: Please wait ...");
        int windowSize = 21;
        int offset = 5;
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, windowSize, offset);
                             
        // Find the contours
        System.out.println("Looking for contours: Please wait ...");
        Contours contours = new Contours(bwImage);
        int minArea = 200;
        contours.findContours(minArea);    
        List<List<List<Integer>>> edges = contours.outerEdges;   
        
        // Organize the contours into a hierarchy
        System.out.println("Organizing Contours into a Hierarchy: Please wait ...");
        Map<Integer, List<Integer>> hierarchy = contours.findOuterEdgeHierarchy();        
          
        // Find the ring centers
        System.out.println("Computing Centers Points: Please wait ...");
        List<List<Double>> discCenters = new ArrayList<>();
        int nRings = nRows*nCols;        
        for (List<Integer> children: hierarchy.values()) {
            
            if (children.size() == nRings) {
                for (Integer childID: children) {
                    List<List<Integer>> childContour = edges.get(childID);
                    discCenters.add(Contours.computeCenter(childContour));
                }
                break;
            }
        }
        if (discCenters.isEmpty()) {
            throw new RuntimeException("Could not find the correct number of discs.");
        }
        
        // Save the disc centers
        System.out.println("Saving the Center Points: Please wait ...");
        String formatString = "%.3f";
        TXT.saveMatrix(discCenters, Double.class, saveDataPath, formatString);
        
       
    }
    
    
}
