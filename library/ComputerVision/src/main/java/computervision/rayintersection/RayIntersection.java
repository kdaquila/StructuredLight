package computervision.rayintersection;

import core.XML;

import java.util.Map;

public class RayIntersection {

    Map<String,Object> config;

    public RayIntersection(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);

        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();

        // Get the configuration data as a map
        config = xml.map;
    }

    private double[][] loadMatrix() {
        String imagesGrayDir = (String) config.get("baseDir");
        String cameraMatrixName = root.selectSingleNode("/config/files/cameraMatrix").getText();
        String cameraMatrixFullPath = baseFolder + "\\" + calibrationMatrixFolder + "\\" + cameraMatrixName;
        String projectorMatrixName = root.selectSingleNode("/config/files/projectorMatrix").getText();
        String projectorMatrixFullPath = baseFolder + "\\" + calibrationMatrixFolder + "\\" + projectorMatrixName;
        double[][] cameraMatrix = CSV.loadDoubleMatrixAsArray(cameraMatrixFullPath);

    }


}
