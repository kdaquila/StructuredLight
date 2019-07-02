package sinewavepattern;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SineWavePattern {    
    
    public static Map<String, double[][]> makeSineArrayStack(int nRows, int nCols, double amplitude, double offset, double waveLength, int nPhaseSteps, String orientation) {
        SortedMap<String, double[][]> imgStack = new TreeMap();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            double[][] newImg = makeSineArray(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLen" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }

    public static Map<String, int[][]> makeSineArrayStack(int nRows, int nCols, int amplitude, int offset, double waveLength, int nPhaseSteps, String orientation) {
        SortedMap<String, int[][]> imgStack = new TreeMap();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            int[][] newImg = makeSineArray(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLen" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }
    
    public static Map<String, short[][]> makeSineArrayStack(int nRows, int nCols, short amplitude, short offset, double waveLength, int nPhaseSteps, String orientation) {
        SortedMap<String, short[][]> imgStack = new TreeMap();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            short[][] newImg = makeSineArray(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLen" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }
    
    public static double[][] makeSineArray(int nRows, int nCols, double amplitude, double offset, double waveLength, double phaseOffset, String orientation) {
                
        // create a new image and get the data array
        double[][] sineWaveArray = new double[nRows][nCols];
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                double gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = sineFunc(amplitude, offset, waveLength, phaseOffset, col);
                } else if (orientation.equals("vertical")) {
                    gray = sineFunc(amplitude, offset, waveLength, phaseOffset, row);
                }

                sineWaveArray[row][col] = gray;
            }
        }

        return sineWaveArray;    
    }
    
    public static int[][] makeSineArray(int nRows, int nCols, int amplitude, int offset, double waveLength, double phaseOffset, String orientation) {
                
        // create a new image and get the data array
        int[][] sineWaveArray = new int[nRows][nCols];
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                int gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = sineFunc(amplitude, offset, waveLength, phaseOffset, col);
                } else if (orientation.equals("vertical")) {
                    gray = sineFunc(amplitude, offset, waveLength, phaseOffset, row);
                }

                sineWaveArray[row][col] = gray;
            }
        }

        return sineWaveArray;    
    }
    
    public static short[][] makeSineArray(int nRows, int nCols, short amplitude, short offset, double waveLength, double phaseOffset, String orientation) {
                
        // create a new image and get the data array
        short[][] sineWaveArray = new short[nRows][nCols];
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                short gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = sineFunc(amplitude, offset, waveLength, phaseOffset, col);
                } else if (orientation.equals("vertical")) {
                    gray = sineFunc(amplitude, offset, waveLength, phaseOffset, row);
                }

                sineWaveArray[row][col] = gray;
            }
        }

        return sineWaveArray;    
    }

    public static double sineFunc(double amplitude, double offset, double waveLength, double phaseOffset, int x){
        return amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset; // the -0.5 is critical
    }
    
    public static int sineFunc(int amplitude, int offset, double waveLength, double phaseOffset, int x){
        return (int)(amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset); // the -0.5 is critical
    }
    
    public static short sineFunc(short amplitude, short offset, double waveLength, double phaseOffset, int x){
        return (short)(amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset); // the -0.5 is critical
    }
    
    
}
