package apps;

import calibrationpattern.rings.WorldRings;
import core.TXT;
import core.XML;
import java.util.List;

public class WorldGridApp {

    public static void main(String[] args) {
        System.out.println("Running the WorldGridApp:");
        
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
        double dx = conf.getDouble("/config/dx");
        double dy = conf.getDouble("/config/dy");
        String formatStr = conf.getString("/config/formatStr"); 
        String saveDataPath = conf.getString("/config/saveDataPath"); 

        System.out.println("Done");
        
        // Compute the world ring grid points
        System.out.print("Computing the points ... ");
        
        List<List<Double>> centers = WorldRings.computeCenters(nRows, nCols, dx, dy);
        
        System.out.println("Done");
        
        // Save the points
        System.out.print("Saving the points ... ");
        
        TXT.saveMatrix(centers, Double.class, saveDataPath, formatStr);
        
        System.out.println("Done");
    }
}
