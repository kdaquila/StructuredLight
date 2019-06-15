package apps;

import calibrationpattern.rings.ImageRings;
import core.Contours;
import static core.Contours.computeArea;
import static core.Contours.contourToPath;
import core.ImageUtils;
import core.LaplacianFilter;
import core.TXT;
import core.XML;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FindRingsApp {
    
    public static void main(String[] args) {
        System.out.println("Running the FindRingsApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);      
        int nRows = conf.getInt("/config/nRows");
        int nCols = conf.getInt("/config/nCols");        
        String formatString = conf.getString("/config/formatString"); 
        String rgbImageDir = conf.getString("/config/rgbImageDir"); 
        String saveDataDir = conf.getString("/config/saveDataDir");
        boolean isSubPixel = conf.getBool("/config/isSubPixel");
        
        System.out.println("Done");
        
        // Find the image paths
        File rgbDir = new File(rgbImageDir);
        String[] rgbImageNames = rgbDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".png") ||
                                  lowerName.endsWith(".jpg") ||
                                  lowerName.endsWith(".jpeg") ||
                                  lowerName.endsWith(".tif") ||
                                  lowerName.endsWith(".tiff"); 
                return isValid;
            }
        });
        
        List<String> skipped = new ArrayList<>();
        List<String> successful = new ArrayList<>();
        for (String rgbImageName: rgbImageNames) 
        {
            System.out.println("\n\nNow Processing Image: " + rgbImageDir + "//" + rgbImageName + "\n");
            
            // Compute the number of rings
            int nRings = nRows*nCols;

            // Load the RGB image
            System.out.print("Loading the RGB image ... "); 
            Path rgbImageFullPath = Paths.get(rgbImageDir).resolve(rgbImageName);
            BufferedImage rgbImage = ImageUtils.load(rgbImageFullPath.toString());

            System.out.println("Done");

            // Convert RGB to gray        
            BufferedImage grayImage = ImageUtils.color2Gray(rgbImage);

            // Adaptive threshold to black and white        
            System.out.print("Thresholding to black and white ... "); 

            int windowSize = 21;
            int offset = 5;
            BufferedImage bwImage = ImageUtils.adaptiveThreshold(grayImage, windowSize, offset);

            System.out.println("Done");
            
            // Find all contours
            System.out.print("Searching for contours ... ");
            Contours contours = new Contours(bwImage);   
            int minArea = 100; 
            List<List<List<Integer>>> edges = contours.findContours(minArea);  

            System.out.println("Done");
            
            // Compute the areas
            List<Double> areas = new ArrayList<>();
            for (List<List<Integer>> contour: edges) {
                Double area = computeArea(contour);
                areas.add(area);
            }
            
            // Compute the Paths
            List<Path2D> paths = new ArrayList<>();
            for (List<List<Integer>> contour: edges) {
                paths.add(contourToPath(contour));
            }

            // Organize the edges into a hierarchy
            System.out.print("Indexing the contours ... ");

            Map<Integer, List<Integer>> hierarchy = Contours.findHierarchy(edges, areas, paths); 

            System.out.println("Done");            
                      
            // Find the rings centers    
            System.out.print("Computing the ring centers ... ");        

            List<List<Double>> ringCenters;
            try {
                ringCenters = ImageRings.computeCenters(edges, hierarchy, nRings);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                skipped.add(rgbImageName);                
                continue;                
            }

            System.out.println("Done"); 

            // Sort ring centers as row-major on a grid
            System.out.print("Sorting the ring centers as grid... ");

            ringCenters = ImageRings.sortCentersRowMajor(ringCenters, nRows, nCols);

            System.out.println("Done");

            if (isSubPixel) {
                
                // Refine ring centers to sub pixel accuracy
                System.out.print("Refining ring centers to subPixel Accuracy... ");
            
                // Compute the average ring outer radius
                Map<String,Double> averageWidths = ImageRings.findAvgRingWidths(edges, hierarchy, nRings);
                
                // Compute the kernal parameters
                Map<String,Double> kernalParams = LaplacianFilter.computeKernalParameters(averageWidths);
                
                // Do the laplacian filtering
                BufferedImage laplacianImg = LaplacianFilter.laplacianFilter(grayImage, kernalParams);
                
                // Find the ring centers to subPixel accuracy  
                ringCenters = ImageRings.refineCenters(ringCenters, laplacianImg, kernalParams);

                System.out.println("Done");                       
            }  
            
            // Save the point centers
            System.out.print("Saving the ring centers data... ");
            
            String imageRootName = rgbImageName.split(Pattern.quote("."))[0];
            String saveDataName = imageRootName + ".txt";
            Path saveDataFullPath = Paths.get(saveDataDir).resolve(saveDataName);
            TXT.saveMatrix(ringCenters, Double.class, saveDataFullPath.toString(), formatString);
            
            System.out.println("Done");
            
            // add to success list
            successful.add(rgbImageName);
            
        }
        
        System.out.println("\n\nSkipped " + skipped.size() + " images: ");
        System.out.println(skipped);
        
        System.out.println("\n\nSuccessful " + successful.size() + " images: ");
        System.out.println(successful);
    }

}
