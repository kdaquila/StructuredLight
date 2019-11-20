package cameraprojectorcalibration;

import core.FITS;
import static core.Print.println;
import core.XML;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import phasemap.TwoAxisTwoFreqPhaseMap;

public class CameraProjectorCalibration {
    
    Map<String,Object> config;  
    
    public CameraProjectorCalibration(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }

    public static void main(String[] args) {
        println("Running the Brightness Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        CameraProjectorCalibration app = new CameraProjectorCalibration(configAbsPath);
        
        String basePath = "";
        
        // Compute the phase map in each View
        SortedMap<String, SortedMap<String, double[][]>> phaseMapStacks = new TreeMap<>();
        SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, double[][]>>>> waveStacks = IO.loadWaveStacks(basePath); 
        double horizontalWaveLength1 = IO.loadHorizontalWaveLength1(basePath);
        double horizontalWaveLength2 = IO.loadHorizontalWaveLength2(basePath);
        double verticalWaveLength1 = IO.loadVerticalWaveLength1(basePath);
        double verticalWaveLength2 = IO.loadVerticalWaveLength2(basePath);
        int nPhaseSteps = IO.loadNumPhaseSteps(basePath);
        for (Map.Entry<String, SortedMap<String, SortedMap<String, SortedMap<String, double[][]>>>> entry: waveStacks.entrySet()) {
            String stackName = entry.getKey();
            SortedMap<String, SortedMap<String, SortedMap<String, double[][]>>> stackSet = entry.getValue();
            SortedMap<String, SortedMap<String, double[][]>> horizontalStackSet = stackSet.get("horizontal");
            SortedMap<String, double[][]> horizontalWaveLength1Stack = horizontalStackSet.get("wavelength1");
            SortedMap<String, double[][]> horizontalWaveLength2Stack = horizontalStackSet.get("wavelength2");
            SortedMap<String, SortedMap<String, double[][]>> verticalStackSet = stackSet.get("vertical");
            SortedMap<String, double[][]> verticalWaveLength1Stack = verticalStackSet.get("wavelength1");
            SortedMap<String, double[][]> verticalWaveLength2Stack = verticalStackSet.get("wavelength2");
            
            double[][] verticalPhaseMap = TwoAxisTwoFreqPhaseMap.computePhaseMap(verticalWaveLength1Stack, verticalWaveLength2Stack, verticalWaveLength1, verticalWaveLength2, nPhaseSteps);
            double[][] horizontalPhaseMap =TwoAxisTwoFreqPhaseMap.computePhaseMap(horizontalWaveLength1Stack, horizontalWaveLength2Stack, horizontalWaveLength1, horizontalWaveLength2, nPhaseSteps); 
            SortedMap<String, double[][]> phaseMaps = new TreeMap<>();
            phaseMaps.put("horizontal", horizontalPhaseMap);
            phaseMaps.put("vertical", verticalPhaseMap);
            phaseMapStacks.put(stackName, phaseMaps);
        }        
        
        // Load all-white image in each View
        
        // Find the ring centers in each View
        
        // Compute the projector pixel coordinates in each View
        
        // Load the world coordinates
        
        // Compute the projector calibration
        
    }
    
}
