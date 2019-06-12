package core;

import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contours {
    
    private BufferedImage bwImg;
    private final byte[] bwImgData;
    public List<List<Integer>> tagMap;
    private final int WIDTH;
    private final int HEIGHT;
    private final int EMPTYPIXEL;
    private final int FILLEDPIXEL;
    private final int NOTAG;
    private final int MARKEDTAG;
    private final int INITIALTAG;
    private int newTag;
    
    public Contours(BufferedImage bwImage)
    {
        EMPTYPIXEL = 255;
        FILLEDPIXEL = 0;
        NOTAG = 0;
        MARKEDTAG = 1;
        INITIALTAG = 2;
        newTag = INITIALTAG;
        WIDTH = bwImage.getWidth();
        HEIGHT = bwImage.getHeight();
        ImageUtils.fillBoundary(bwImage, 255);
        bwImg = bwImage;
        bwImgData = ((DataBufferByte)bwImg.getData().getDataBuffer()).getData();
        
        // initialize Tag Map
        tagMap = new ArrayList<>();
        for (int row_num = 0; row_num < HEIGHT; row_num++)
        {
            List<Integer> newRow = new ArrayList<>();
            for (int col_num = 0; col_num < WIDTH; col_num++)
            {
                newRow.add(NOTAG);
            }            
            tagMap.add(newRow);
        }     
                
    }
    
    public List<List<List<Integer>>> findContours() {
        int minArea = 0;
        return findContours(minArea);
    }
    
    public List<List<List<Integer>>> findContours(double minArea) {
        
        List<List<List<Integer>>> contours = new ArrayList<>();
        
        ImageUtils.fillBoundary(bwImg, EMPTYPIXEL);
        
        for (int y = 1; y < HEIGHT-1; y++)
        {
            for (int x = 1; x < WIDTH-1; x++)
            {                
                // get local pixels
                int bw = (bwImgData[x + y*WIDTH] & 0xFF);
                int bwUp = (bwImgData[x + (y - 1)*WIDTH] & 0xFF);
                int bwDown = (bwImgData[x + (y + 1)*WIDTH] & 0xFF);
                
                // get local tags
                int tag = tagMap.get(y).get(x);      
                int tagDown = tagMap.get(y+1).get(x);
                
                // This is a new external edge
                if (bw == FILLEDPIXEL &&
                    bwUp == EMPTYPIXEL &&
                    tag == NOTAG )
                {
                    int startAngle = 315; //degrees
                    List<List<Integer>> newEdge = followEdge(x, y, startAngle, newTag);
                    newTag += 1;
                    // store the contour
                    if (Contours.computeArea(newEdge) >= minArea) {
                        contours.add(newEdge);
                    }                    
                }
                // This is a new internal edge
                else if (bw == FILLEDPIXEL &&
                         bwDown == EMPTYPIXEL &&
                         tagDown != MARKEDTAG)
                {
                    int startAngle = 135; //degrees
                    List<List<Integer>> newEdge = followEdge(x, y, startAngle, newTag);                    
                    newTag += 1;
                    // store the contour
                    if (Contours.computeArea(newEdge) >= minArea) {
                        contours.add(newEdge);
                    } 
                } 
            }
        }
        
        return contours;
    }
    
    private List<List<Integer>> followEdge(int startX, int startY, int startAngle, int tag)
    {
        List<List<Integer>> outputEdge = new ArrayList<>();
         
        // First Index
        int firstIndex = WIDTH*startY + startX;
        tagMap.get(startY).set(startX, tag);
        outputEdge.add(Arrays.asList(startX, startY));
        
        // Second Index
        int secondIndex = findNeighbor(firstIndex, startAngle, FILLEDPIXEL);        
        if (secondIndex == -1) {
            return outputEdge; // can't find second index (i.e. the contour is a single pixel)
        }        
        
        // Current Index
        int currIndex = secondIndex;
        
        // Next Index
        LocalPixels localPixels = new LocalPixels(secondIndex, WIDTH, HEIGHT);
        int searchAngle = (localPixels.getAngleTo(firstIndex) + 90)%360;
        int nextIndex = findNeighbor(secondIndex, searchAngle, FILLEDPIXEL);
        
        while(currIndex != firstIndex  || nextIndex != secondIndex)
        {
            // Store
            int currY = currIndex/WIDTH;
            int currX = currIndex - currY*WIDTH;
            outputEdge.add(Arrays.asList(currX, currY));
            tagMap.get(currY).set(currX, tag);
            
            // Update
            localPixels = new LocalPixels(nextIndex, WIDTH, HEIGHT);
            searchAngle = (localPixels.getAngleTo(currIndex) + 90)%360;
            currIndex = nextIndex;            
            nextIndex = findNeighbor(currIndex, searchAngle, FILLEDPIXEL);
        }

        return outputEdge;
    }
    
    private int findNeighbor(int currentIndex, int startAngle, int value)
    {
        LocalPixels localPixels = new LocalPixels(currentIndex, WIDTH, HEIGHT);
        List<Integer> localIndices = localPixels.getIndices(startAngle); 
            
        for (Integer localIndex: localIndices)
        {            
            int localY = localIndex/WIDTH;
            int localX = localIndex - localY*WIDTH;
            if (localX < 0 || localX > (WIDTH-1) || localY < 0 || localY > (HEIGHT-1)) {
                continue;
            }
            
            if ((bwImgData[localIndex] & 0xFF) == value)
            {
                return localIndex;
            } 
            else
            {
                tagMap.get(localY).set(localX, MARKEDTAG);
            }
        }
        return -1; // could not find a neighbor with the correct value
    }
    
    public static List<Double> computeCenter(List<List<Integer>> contour)
    {
        double sumX = 0;
        double n = 0;
        double sumY = 0;
        
        for (List<Integer> pt: contour)
        {
            sumX += pt.get(0);            
            sumY += pt.get(1); 
            n += 1;
        }        
        
        List<Double> center = new ArrayList<>();
        center.add(sumX/n);
        center.add(sumY/n);
        
        return center;
    }
    
    public static List<List<Double>> computeCenters(List<List<List<Integer>>> contours, List<Integer> ids) {
        
        List<List<Double>> centers = new ArrayList<>();
        for (Integer id: ids) {
            List<List<Integer>> contour = contours.get(id);
            List<Double> center = Contours.computeCenter(contour);
            centers.add(center);
        }
        
        return centers;
    }
    
    public static Integer findParentID(Map<Integer, List<Integer>> hierarchy, int nChildren) {
        Integer parentID = null;
        int maxFound = 0;
        for (Integer id: hierarchy.keySet()) {            
            int numFound = hierarchy.get(id).size();
            if (numFound > maxFound) {
                maxFound = numFound;
            }
            if (numFound == nChildren) {
                parentID = id;
                break;
            }
        } 
        
        return parentID;
    }
    
    public static List<Integer> computeBoundingBox(List<List<Integer>> contour) {
        
        int nPts = contour.size();
        List<Integer> xList = new ArrayList<>(nPts);
        List<Integer> yList = new ArrayList<>(nPts);
        for (List<Integer> pt: contour) {
            xList.add(pt.get(0));
            yList.add(pt.get(1));
        }
        
        int xMax = Collections.max(xList);
        int xMin = Collections.min(xList);
        int yMax = Collections.max(yList);
        int yMin = Collections.min(yList);
        
        int x = xMin;
        int y = yMin;
        int w = xMax - xMin;
        int h = yMax - yMin;
        
        List<Integer> box = new ArrayList<>(4);
        box.add(x);
        box.add(y);
        box.add(w);
        box.add(h);
        
        return box;
    }
    
    public static Double computeAverageWidth(List<List<List<Integer>>> contours, List<Integer> ids) {
        double sum = 0;
        for(Integer id: ids) {        
            List<List<Integer>> contour = contours.get(id);
            List<Integer> box = Contours.computeBoundingBox(contour);
            sum += box.get(2);
        }
        return sum/contours.size();
    }
    
    public static Double computeArea(List<List<Integer>> contour) {
        
        double area = 0.0;
        int nPts = contour.size();
        for (int i = 0; i < nPts; i++) {
            int y_curr = contour.get(i).get(1);
            int y_next = contour.get((i+1)%nPts).get(1);
            int x_curr = contour.get(i).get(0);
            int x_next = contour.get((i+1)%nPts).get(0);
            area += x_curr*y_next - x_next*y_curr;
        }
        return 0.5*Math.abs(area);
    }
    
    public static List<List<Integer>> findConvexHull(List<List<Integer>> contour)
    {        
        List<List<Integer>> hull = new ArrayList<>();
        
        int nPts = contour.size();
        
        // find the leftmost point's index
        int left_index = 0;
        for (int index = 1; index < nPts; index++)
        {
            if (contour.get(index).get(0) < contour.get(left_index).get(0))
            {
                left_index = index;
            }
        }
        
        // set the first point
        int a = left_index;
        
        // look for the other points
        boolean isDone = false;
        while (isDone == false)
        {
            // update point b
            int b = (a + 1)%nPts;
            for (int c = 0; c < nPts; c++)
            {
                // compute area of triangle (a, b, c) using
                double ax = contour.get(a).get(0);
                double ay = contour.get(a).get(1);
                double bx = contour.get(b).get(0);
                double by = contour.get(b).get(1);
                double cx = contour.get(c).get(0);
                double cy = contour.get(c).get(1);
                double area = ((bx-ax)*(cy-ay)-(by-ay)*(cx-ax));
                
                if (area < 0)
                {
                    b = c;
                }
            }
            
            // save point a
            List<Integer> newPoint = new ArrayList<>(2);
            newPoint.add(contour.get(a).get(0));
            newPoint.add(contour.get(a).get(1));
            hull.add(newPoint);
            
            // move point a
            a = b;
            if (a == left_index) {
                isDone = true;
            }
        }
        
        return hull;
    }     
    
    public static Map<String,Map<Integer,List<Integer>>> findHierarchy_batch(Map<String,List<List<List<Integer>>>> contourSets) {
        Map<String,Map<Integer,List<Integer>>> output = new HashMap<>();
        for (String name: contourSets.keySet()) {
            List<List<List<Integer>>> contours = contourSets.get(name);
            Map<Integer,List<Integer>> hierarchy = Contours.findHierarchy(contours);
            output.put(name, hierarchy);
        }
        return output;
    }
    
    public static Map<Integer,List<Integer>> findHierarchy(List<List<List<Integer>>> contours) {
        
        // Initialize each contour as a parent
        Map<Integer, List<Integer>> hierarchy = new HashMap<>();
        for (int index = 0; index < contours.size(); index++) {
            hierarchy.put(index, new ArrayList<>());
        }
        
        // Find the direct parent of each contour
        for (int contourIndex = 0; contourIndex < contours.size(); contourIndex++) {
            
            // Get the contour
            List<List<Integer>> contour = contours.get(contourIndex);                                  
            
            // Find the contour's area
            double contourArea = Contours.computeArea(contour);
            
            
            // Check each candidate parent contour
            Double minArea = Double.MAX_VALUE;
            Integer parentIndex = null;
            for (int candidateParentIndex = 0; candidateParentIndex < contours.size(); candidateParentIndex++) {
                
                // Skip the contour itself
                if (candidateParentIndex == contourIndex) {
                    continue;
                }
                
                // Get the candidate parent contour
                List<List<Integer>> candidateParentContour = contours.get(candidateParentIndex);
                
                
                // Stop if the candidate parent is not a contour
                if (candidateParentContour == null) {
                    continue;
                }                
                
                // Find the parent contours's area
                double parentContourArea = Contours.computeArea(candidateParentContour);
                                
                // Store the candidate parent contour as a path
                Path2D candidateParentContourPath = new Path2D.Double();
                List<Integer> firstPt = candidateParentContour.get(0);
                candidateParentContourPath.moveTo(firstPt.get(0), firstPt.get(1));
                for (int pt_index = 1; pt_index < candidateParentContour.size(); pt_index++) {
                    List<Integer> point = candidateParentContour.get(pt_index);
                    candidateParentContourPath.lineTo(point.get(0), point.get(1));
                }
                
                // check if candidate parent contour contains the contour
                if (parentContourArea > contourArea &&
                    candidateParentContourPath.contains(contour.get(0).get(0), contour.get(0).get(1)) ) {
                    
                    // check if candidate parent is smaller than previous candidate parents
                    double candidateParentArea = Contours.computeArea(candidateParentContour);
                    if (candidateParentArea < minArea) {
                        minArea = candidateParentArea;
                        parentIndex = candidateParentIndex;
                    }    
                }                
            }
            
            // Associate this contour with its parent
            if (parentIndex != null) {
                hierarchy.get(parentIndex).add(contourIndex);
            }    
        }
        
        return hierarchy;
    }   
    
    public static Map<String,List<List<List<Integer>>>> findContours_batch(Map<String,BufferedImage> bwImages) {
        int minArea = 0;
        return findContours_batch(bwImages, minArea);
    }
    
    public static Map<String,List<List<List<Integer>>>> findContours_batch(Map<String,BufferedImage> bwImages, double minArea) {
        Map<String,List<List<List<Integer>>>> output = new HashMap<>();
        for (String imgName: bwImages.keySet()) {
            BufferedImage bwImg = bwImages.get(imgName);
            Contours contourObj = new Contours(bwImg);
            List<List<List<Integer>>> contourSet = contourObj.findContours(minArea);
            output.put(imgName, contourSet);
        }
        return output;
    }
    
    public class LocalPixels
    {    
        int imgWidth;
        int imgHeight;
        int currentIndex;
        Integer[] localAngles;
        List<Integer> localIndices;

        public LocalPixels(int x, int y, int imgWidth, int imgHeight)
        {
            this.imgWidth = imgWidth;
            this.imgHeight = imgHeight;
            currentIndex = y*imgWidth + x;
            localAngles = new Integer[8];
            localAngles[0] = 0;
            localAngles[1] = 45;
            localAngles[2] = 90;
            localAngles[3] = 135;
            localAngles[4] = 180;
            localAngles[5] = 225;
            localAngles[6] = 270;
            localAngles[7] = 315;        
            localIndices = new ArrayList<>(8);
            localIndices.add((y + 0)*imgWidth + (x + 1));        
            localIndices.add((y + 1)*imgWidth + (x + 1));        
            localIndices.add((y + 1)*imgWidth + (x + 0));
            localIndices.add((y + 1)*imgWidth + (x - 1));
            localIndices.add((y + 0)*imgWidth + (x - 1));
            localIndices.add((y - 1)*imgWidth + (x - 1));
            localIndices.add((y - 1)*imgWidth + (x + 0));
            localIndices.add((y - 1)*imgWidth + (x + 1));
        }

        public LocalPixels(int index, int imgWidth, int imgHeight)
        {
            this.imgWidth = imgWidth;
            this.imgHeight = imgHeight;
            currentIndex = index;        
            localAngles = new Integer[8];
            localIndices = new ArrayList<>(8);
            localAngles[0] = 0;
            localAngles[1] = 45;
            localAngles[2] = 90;
            localAngles[3] = 135;
            localAngles[4] = 180;
            localAngles[5] = 225;
            localAngles[6] = 270;
            localAngles[7] = 315;
            int y = index/imgWidth;
            int x = index - y*imgWidth;
            localIndices.add((y + 0)*imgWidth + (x + 1));        
            localIndices.add((y + 1)*imgWidth + (x + 1));        
            localIndices.add((y + 1)*imgWidth + (x + 0));
            localIndices.add((y + 1)*imgWidth + (x - 1));
            localIndices.add((y + 0)*imgWidth + (x - 1));
            localIndices.add((y - 1)*imgWidth + (x - 1));
            localIndices.add((y - 1)*imgWidth + (x + 0));
            localIndices.add((y - 1)*imgWidth + (x + 1));
        }

        public int getAngleTo(int index)
        {
            int key = localIndices.indexOf(index);
            return localAngles[key];
        }

        public List<Integer> getIndices(int startAngle)
        {
            startAngle = startAngle%360;
            int offset = Arrays.binarySearch(localAngles, startAngle);        
            List<Integer> output = new ArrayList<>(localIndices);
            Collections.rotate(output, -1*offset);
            return output;
        }
    }    
}
