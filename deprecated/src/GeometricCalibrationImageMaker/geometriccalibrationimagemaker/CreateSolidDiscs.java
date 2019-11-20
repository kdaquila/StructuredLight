package geometriccalibrationimagemaker;

import calibrationpattern.soliddiscs.ModelDiscs;
import core.ImageUtils;
import static core.Print.println;
import core.XML;
import java.awt.Color;
import java.util.Map;

/**
 * This class is used to create a calibration pattern consisting of a rectangular 
 * grid of discs that are contained within a frame. The parameters are read
 * from a XML configuration file.
 * @author kfd18
 */
public class CreateSolidDiscs {
    
    public static void main(String[] args) {
        println("Running the CreateRingsApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        Map<String, Object> config = XML.loadMap(configPath, "config");

        // Render the calibration pattern model
        println("Rendering the disc pattern ... ");
        
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int borderWidth = (Integer) config.get("borderWidth");
        int halfPitch = (Integer) config.get("halfPitch");
        ModelDiscs model = new ModelDiscs(nRows, nCols, borderWidth, halfPitch);
        int backgroundColor = (Integer) config.get("backgroundColor"); 
        model.drawFill(new Color(backgroundColor)); 
        int borderColor = (Integer) config.get("borderColor"); 
        model.drawBorder(new Color(borderColor));   
        int discRadius = (Integer) config.get("discRadius"); 
        model.drawDiscs(discRadius);
        
        // Save the model
        println("Saving the disc pattern ... ");   
        String patternDir = (String) config.get("patternDir");
        String patternFilename = (String) config.get("patternFilename");
        ImageUtils.save(model.img, patternDir, patternFilename);
    }        
}
