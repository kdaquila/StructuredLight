package calibrationpattern.rings;

import core.Contours;
import core.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class ImagePoints {
           
    public static List<List<Double>> find(BufferedImage rgbImg, int nPts) {

        // Convert RGB to gray        
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImg);

        // Adaptive threshold to black and white
        int windowSize = 21;
        int offset = 5;
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, windowSize, offset);

        // Find all outer edges
        Contours contours = new Contours(bwImage);
        int minArea = 200;            
        List<List<List<Integer>>> outerEdges = contours.findContours(minArea);  

        // Organize the edges into a hierarchy
        Map<Integer, List<Integer>> hierarchy = Contours.findHierarchy(outerEdges); 
        
        // Find the rings' parent
        int nChildren = nPts;
        Integer parentID = Contours.findParentID(hierarchy, nChildren);        
        
        // Compute the rings' centers        
        List<Integer> ringIDs = hierarchy.get(parentID);
        List<List<Double>> ringCenters = Contours.computeCenters(outerEdges, ringIDs);
        
        return ringCenters;
    }
    

    
    
    
    
    
    
}
