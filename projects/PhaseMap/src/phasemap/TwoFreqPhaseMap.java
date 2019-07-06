package phasemap;

import core.FITS;
import core.ImageStackUtils;
import static core.Print.println;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class TwoFreqPhaseMap {
    
    Map<String,Object> config;  
    
    public TwoFreqPhaseMap(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public void computePhaseMap(){
        
        
        // load image stacks
        String sineWavePhotoSetDir1 = (String) config.get("sineWavePhotoSetDir1");
        SortedMap<String, double[][]> grayStack1 = FITS.loadDoubleArray2D_Batch(sineWavePhotoSetDir1);
        String sineWavePhotoSetDir2 = (String) config.get("sineWavePhotoSetDir2");
        SortedMap<String, double[][]> grayStack2 = FITS.loadDoubleArray2D_Batch(sineWavePhotoSetDir2);

        // blur the images slightly
        int blurRadius = 3;
        SortedMap<String, double[][]> blurGrayStack1 = ImageStackUtils.blurStack(grayStack1, blurRadius);
        SortedMap<String, double[][]> blurGrayStack2 = ImageStackUtils.blurStack(grayStack2, blurRadius);
        
        // compute the phase offsets
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");
        List<Double> phaseOffsets = new ArrayList<>(nPhaseSteps);
        for (double phase = 0; phase < 2*Math.PI; phase += (2*Math.PI/nPhaseSteps)) {
            phaseOffsets.add(-phase);
        }
        double waveLength1 = (Double) config.get("waveLength1");        
        double waveLength2 = (Double) config.get("waveLength2");  

        // compute wrapped phase maps
        double[][] wrappedPhaseMap1 = computeWrappedPhaseMap(grayStack1, phaseOffsets);
        double[][] wrappedPhaseMap2 = computeWrappedPhaseMap(grayStack2, phaseOffsets);
        
        // save wrapped phase maps
        String phaseMapDir = (String) config.get("phaseMapDir");
        String wrappedPhaseMapName1 = "wrappedPhaseMap_" + waveLength1 + ".FITS";
        FITS.saveImage(wrappedPhaseMap1, phaseMapDir, wrappedPhaseMapName1);
        String wrappedPhaseMapName2 = "wrappedPhaseMap_" + waveLength2 + ".FITS";
        FITS.saveImage(wrappedPhaseMap1, phaseMapDir, wrappedPhaseMapName2);
                
        // compute unwrapped phase map
        double[][] unwrappedPhaseMap = computeUnwrappedPhaseMap(wrappedPhaseMap1, wrappedPhaseMap2);
        
        // save unwrapped phase maps
        String unWrappedPhaseMapName = "unWrappedPhaseMap.FITS";
        FITS.saveImage(unwrappedPhaseMap, phaseMapDir, unWrappedPhaseMapName);
        
        // compute absolute phase map
        double unWrappedWaveLength = waveLength1*waveLength2/Math.abs(waveLength1-waveLength2);
        double[][] absolutePhaseMap = computeAbsPhaseMap(wrappedPhaseMap1, waveLength1, unwrappedPhaseMap, unWrappedWaveLength);
                
        // save the absolute phase map
        String absolutePhaseMapName = "absolutePhaseMap.FITS";
        FITS.saveImage(absolutePhaseMap, phaseMapDir, absolutePhaseMapName);
    }
    
    private double[][] computeWrappedPhaseMap(SortedMap<String, double[][]> stack, List<Double> phaseOffsets){        
        

        // get image dimensions
        int nRows = stack.get((new ArrayList<>(stack.keySet())).get(0)).length;
        int nCols = stack.get((new ArrayList<>(stack.keySet())).get(0))[0].length;        
        int nSlices = stack.size();
        
        double[][] output = new double[nRows][nCols];

        // compute phase at each pixel
        List<String> imgNames = new ArrayList<>(stack.keySet());
        for(int row_num = 0; row_num < nRows; row_num++){
            for (int col_num=0; col_num < nCols; col_num++){
                double numerator = 0;
                double denominator = 0;
                for (int slice_num = 0; slice_num < nSlices; slice_num++){
                    String imgName = imgNames.get(slice_num);
                    double gray = stack.get(imgName)[row_num][col_num];
                    double phase = phaseOffsets.get(slice_num);
                    numerator += gray*Math.cos(phase);
                    denominator += gray*Math.sin(phase);
                }
                output[row_num][col_num] = Math.atan2(numerator, denominator);                
            }
        }

        return output;
    }
    
    private double[][] computeUnwrappedPhaseMap(double[][] phaseMap1, double[][] phaseMap2){
        int nCols = phaseMap1[0].length;
        int nRows = phaseMap1.length;
        double[][] output = new double[nRows][nCols];
        for(int row_num = 0; row_num < nRows; row_num++){
            for (int col_num=0; col_num < nCols; col_num++){
                double phase1 = phaseMap1[row_num][col_num];
                double phase2 = phaseMap2[row_num][col_num];
                output[row_num][col_num] = mod((phase1-phase2),2*Math.PI);
            }
        }        
        return output;
    }
    
    private double[][] computeAbsPhaseMap(double[][] phaseMap1, Double waveLength1, double[][] phaseMap3, Double waveLength3) {
        int nCols = phaseMap1[0].length;
        int nRows = phaseMap1.length;
        double[][] output = new double[nRows][nCols];
        for(int row_num = 0; row_num < nRows; row_num++){
            for (int col_num=0; col_num < nCols; col_num++){
                double phase1 = phaseMap1[row_num][col_num];
                double phase3 = phaseMap3[row_num][col_num];
                long kFactor = Math.round((phase3*waveLength3/waveLength1-phase1)/(2*Math.PI));
                output[row_num][col_num] = phase1 + kFactor*2*Math.PI;
            }
        }        
        return output;        
    }
    
    private double mod(double x, double y)
    {
        double result = x % y;
        if (result < 0)
        {
            result += y;
        }
        return result;
    }

    public static void main(String[] args) {
        println("Running the Phase Map App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configPath = args[0];
        
        // Run the app
        TwoFreqPhaseMap twoFreqPhaseMap = new TwoFreqPhaseMap(configPath);
        twoFreqPhaseMap.computePhaseMap();
        
        
    }
    
}
