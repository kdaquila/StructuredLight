package apps;

import calibrationpattern.rings.Model;
import core.PNG;
import core.XML;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.Node;

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
        
        // Load the configuration file
        System.out.print("Loading the configuration ... ");
        
        Element root = XML.loadXML(configPath);        
        
        // Define the configuration keys
        List<String> configKeys = new ArrayList<>();
        configKeys.add("/config/nRows");
        configKeys.add("/config/nCols");
        configKeys.add("/config/borderWidth");
        configKeys.add("/config/shapeRadius");
        configKeys.add("/config/ringOuterRadius");
        configKeys.add("/config/ringInnerRadius");
        configKeys.add("/config/dpi");
        configKeys.add("/config/borderColor");
        configKeys.add("/config/backgroundColor");
        configKeys.add("/config/ringColor");
        configKeys.add("/config/savePath");
        
        // Load and Validate the configuration values        
        Map<String, String> configMap = new HashMap<>();
        for (String key: configKeys) {
            Node valueNode = root.selectSingleNode(key);
            
            if (valueNode == null) {
                throw new RuntimeException("Could not load configuration variable: " + key);
            }
            
            String value = valueNode.getText();
            configMap.put(key, value);                     
        }
        
        System.out.println("Done");
        
        // Set the configuation variables, converting type if needed
        int nRows = Integer.parseInt(configMap.get("/config/nRows"));
        int nCols = Integer.parseInt(configMap.get("/config/nCols"));
        int borderWidth = Integer.parseInt(configMap.get("/config/borderWidth"));
        int shapeRadius = Integer.parseInt(configMap.get("/config/shapeRadius"));
        int backgroundColor = Integer.parseInt(configMap.get("/config/backgroundColor"), 16);        
        int borderColor = Integer.parseInt(configMap.get("/config/borderColor"), 16);        
        int ringColor = Integer.parseInt(configMap.get("/config/ringColor"), 16);
        int ringOuterRadius = Integer.parseInt(configMap.get("/config/ringOuterRadius"));
        int ringInnerRadius = Integer.parseInt(configMap.get("/config/ringInnerRadius"));
        int dpi = Integer.parseInt(configMap.get("/config/dpi"));
        
        String savePath = configMap.get("/config/savePath"); 

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
        png.setDPI(dpi);        
        png.save(model.img, savePath);    
        
        System.out.println("Done");
    }        
}
