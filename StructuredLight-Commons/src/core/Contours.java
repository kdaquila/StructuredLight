package core;

import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
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
    
    public List<List<List<Integer>>> findContours(int minArea) {
        
        List<List<List<Integer>>> outerEdges = new ArrayList<>();
        
        ImageUtil.fillBoundary(bwImg, EMPTYPIXEL);
        
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
                        outerEdges.add(newEdge);
                    }                    
                }
                // This is a new internal edge
                else if (bw == FILLEDPIXEL &&
                         bwDown == EMPTYPIXEL &&
                         tagDown != MARKEDTAG)
                {
                    int startAngle = 135; //degrees
                    followEdge(x, y, startAngle, newTag);                    
                    newTag += 1;
                    // don't store the contour
                } 
            }
        }
        
        return outerEdges;
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
        int parentID = 0;
        for (Integer id: hierarchy.keySet()) {
            if (hierarchy.get(id).size() == nChildren) {
                parentID = id;
                break;
            } else {
                throw new RuntimeException("Could not find a parent contour with the correct number of child contours.");
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
    
    public BufferedImage drawEdges(List<List<List<Integer>>> contours)
    {
        BufferedImage rgbImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        int[] rgbBuffer = ((DataBufferInt)rgbImage.getRaster().getDataBuffer()).getData();
        List<Integer> colors = new ArrayList<>();
        colors.add(0xFF00FF);
        colors.add(0xFFFF00);
        colors.add(0x00FFFF);
        int color_index = 0;
        for (List<List<Integer>> contour: contours)
        {
            for (List<Integer> point: contour)
            {
                int x = point.get(0);
                int y = point.get(1); 
                int index = y*WIDTH + x;
                rgbBuffer[index] = colors.get(color_index);                
            }
            color_index = (color_index + 1)%colors.size();
        }
        return rgbImage;
    }    

    public static Map<Integer,List<Integer>> findHierarchy(List<List<List<Integer>>> contours) {
        
        Map<Integer, List<Integer>> hierarchy = new HashMap<>();
        
        // Check each Alpha contour
        for (int alphaID = 0; alphaID < contours.size(); alphaID++) {
            
            // Store the Alpha Contour as a Path
            Path2D alphaPath = new Path2D.Double();
            List<Integer> firstPt = contours.get(alphaID).get(0);
            alphaPath.moveTo(firstPt.get(0), firstPt.get(1));
            for (int pt_index = 1; pt_index < contours.get(alphaID).size(); pt_index++) {
                List<Integer> point = contours.get(alphaID).get(pt_index);
                alphaPath.lineTo(point.get(0), point.get(1));
            }
            
            // Check each Beta contour
            for (int betaID = 0; betaID < contours.size(); betaID++) {
                
                if (betaID != alphaID) {
                    
                    // Find the Beta Contour Bounding Box
                    List<List<Integer>> betaContour = contours.get(betaID);
                    List<Integer> box = Contours.computeBoundingBox(betaContour);
                    int x = box.get(0);
                    int y = box.get(1);
                    int width = box.get(2);
                    int height = box.get(3);
                    Rectangle boxRect = new Rectangle(x, y, width, height);
                    
                    // Check if Beta is Inside Alpha                    
                    if (alphaPath.contains(boxRect)) {
                        
                        // Existing Parent Contour
                        if (hierarchy.containsKey(alphaID)) {
                            List<Integer> children = hierarchy.get(alphaID);
                            
                            // Check each existing child
                            boolean isBetaInsideExisting = false;
                            for (Integer existingID: children) {
                                List<List<Integer>> existingContour = contours.get(existingID);
                                List<Integer> existingBox = Contours.computeBoundingBox(existingContour);
                                int existingX = existingBox.get(0);
                                int existingY = existingBox.get(1);
                                int existingW = existingBox.get(2);
                                int existingH = existingBox.get(3);
                                Rectangle existingContourBoxRect = new Rectangle(existingX, existingY, existingW, existingH);
                                
                                // Check if Beta is Inside Existing
                                if (existingContourBoxRect.contains(boxRect)) {
                                    isBetaInsideExisting = true;
                                    break;
                                }                                
                            }
                            if (isBetaInsideExisting == false) {
                                children.add(betaID);
                            }
                            
                        } 
                        // New Parent Contour
                        else {
                            List<Integer> children = new ArrayList<>();
                            children.add(betaID);
                            hierarchy.put(alphaID, children);
                        }
                    }
                }
            }
        }
        
        return hierarchy;
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
