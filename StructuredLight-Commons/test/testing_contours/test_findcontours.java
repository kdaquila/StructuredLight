package testing_contours;

import java.awt.image.BufferedImage;
import structuredlightcommons.ImageUtil;
import structuredlightcommons.Contours;

public class test_findcontours {

    public static void main(String[] args) {
       testing_findContours();
    }
    
    public static void testing_findContours()
    {
        System.out.println("Testing: findRegions()");
        
        String openFolder = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\2018_12_13_CameraCalibrationImages\\Dataset1\\Extracted_Frames\\RGB";
        String openFilename = "9222.png";
        BufferedImage rgbImage = ImageUtil.load(openFolder, openFilename);
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImage);
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, 21, 21);
        ImageUtil.fillBoundary(bwImage, 255);       
        
        String saveFolder = "C:\\Users\\kfd18\\Downloads";
        ImageUtil.save(bwImage, saveFolder, "bw_img.png");
        
        Contours myRegions = new Contours(bwImage);
        myRegions.findRegions();
        int nRegionsFound = myRegions.edges.size();
        System.out.println("Found " + String.valueOf(nRegionsFound) + " regions");
        
        BufferedImage contourImage = myRegions.drawEdges();
        ImageUtil.save(contourImage, saveFolder, "contour_img.png");
        
        BufferedImage tagImage = ImageUtil.ListToGrayImage_Integer(myRegions.tagMap);
        ImageUtil.save(tagImage, saveFolder, "tag_img.png");
    }

}
