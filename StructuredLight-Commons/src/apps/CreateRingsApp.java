package apps;

import calibrationpattern.rings.Model;
import core.PNG;
import core.XML;
import java.awt.Color;

/**
 * This class is used to create a calibration pattern consisting of a rectangular 
 * grid of rings that are contained within a frame. The parameters are read
 * from a XML configuration file.
 * @author kfd18
 */
public class CreateRingsApp {
    
    public static void main(String[] args) {
        
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
        int borderWidth = conf.getInt("/config/borderWidth");
        int shapeRadius = conf.getInt("/config/shapeRadius");
        int backgroundColor = conf.getInt("/config/backgroundColor", 16);        
        int borderColor = conf.getInt("/config/borderColor", 16);        
        int ringColor = conf.getInt("/config/ringColor", 16);
        int ringOuterRadius = conf.getInt("/config/ringOuterRadius");
        int ringInnerRadius = conf.getInt("/config/ringInnerRadius");
        String savePath = conf.getString("/config/savePath"); 
        
        System.out.println("Done");

        // Render the calibration pattern model
        System.out.print("Rendering the ring pattern ... ");
        
        Model model = new Model(nRows, nCols, borderWidth, shapeRadius);  
        model.drawFill(new Color(backgroundColor));   
        model.drawBorder(new Color(borderColor));   
        model.drawRings(new Color(ringColor), ringOuterRadius, ringInnerRadius);
        
        System.out.println("Done");
        
        // Save the model
        System.out.print("Saving the ring pattern ... ");
        
        PNG png = new PNG();       
        png.save(model.img, savePath);    
        
        System.out.println("Done");
    }        
}
