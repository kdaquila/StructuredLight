package apps;

import cameracalibration.IntrinsicMatrix;
import core.ArrayUtils;
import core.TXT;
import core.XML;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IntrinsicMatrixApp {

    public static void main(String[] args) {
        System.out.println("Running the IntrinsicMatrixApp:");

        // Validate arguments
        if (args.length == 0) {
            String errorMessage = "Please provide one argument which is the path to the XML configuration file";
            errorMessage += "Found path: " + args[0];
            throw new IllegalArgumentException(errorMessage);
        } 

        // Parse the arguments
        String configPath = args[0];

        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        XML conf = new XML(configPath);          
        String homographyDir = conf.getString("/config/homographyDir"); 
        String intrinsicMatrixPath = conf.getString("/config/intrinsicMatrixPath");
        String formatStr = conf.getString("/conf/formatString");
        System.out.println("Done");
        
        // Find the homography paths
        File homographyDir_file = new File(homographyDir);
        String[] homographyFilenames = homographyDir_file.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".txt") ||
                                  lowerName.endsWith(".csv"); 
                return isValid;
            }
        });
        
        // Validate filenames
        if (homographyFilenames == null || homographyFilenames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        // Load the homographies
        List<List<List<Double>>> homographies = new ArrayList<>(homographyFilenames.length);
        for (String homographyFilename: homographyFilenames) {
            String homographyFullPath = Paths.get(homographyDir).resolve(homographyFilename).toString();
            homographies.add(TXT.loadMatrix(homographyFullPath, Double.class));            
        }         
        
        // Compute the intrinsic matrix
        System.out.print("Computing the intrinsic camera matrix ... ");
        List<List<Double>> K = IntrinsicMatrix.compute(homographies);
        System.out.println("Done");
        
        // TODO remove after debug
        System.out.println("Matrix K: ");
        ArrayUtils.printList_Double2D(ArrayUtils.ArrayToList_Double2D(K.getData()), "%+010.3f");     
        
        // Save the intrinsic matrix
        TXT.saveMatrix(K, Double.class, intrinsicMatrixPath, formatStr);
        
    }
}
