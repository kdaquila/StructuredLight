package phaseimages;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.Map;

public class SineWaveImage {    
    
    public static Map<String, BufferedImage> makeSineWaveImageStack(int nRows, int nCols, double amplitude, double offset, double waveLength, int nPhaseSteps, String orientation) {
        Map<String, BufferedImage> imgStack = new HashMap<>();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            BufferedImage newImg = makeSineWaveImage(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLen" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }
    
    
    public static BufferedImage makeSineWaveImage(int nRows, int nCols, double amplitude, double offset, double waveLength, double phaseOffset, String orientation) {
        
        // stop if sine function will go out of range
        if ( (amplitude + offset > 255) || (offset - amplitude < 0) ) {
            throw new IllegalArgumentException("Sine values out of range 0-255. Amplitude + Offset = " + (amplitude + offset));
        }
        
        // create a new image and get the data array
        BufferedImage sineWaveImg = new BufferedImage(nCols, nRows, BufferedImage.TYPE_BYTE_GRAY);
        byte[] sineWaveArray = ((DataBufferByte) sineWaveImg.getRaster().getDataBuffer()).getData();
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                byte gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = (byte) Math.round(sineFunc(amplitude, offset, waveLength, phaseOffset, col));
                } else if (orientation.equals("vertical")) {
                    gray = (byte) Math.round(sineFunc(amplitude, offset, waveLength, phaseOffset, row));
                }

                int index = row*nCols + col;
                sineWaveArray[index] = gray;
            }
        }

        return sineWaveImg;    
    }
    
    public static double sineFunc(double amplitude, double offset, double waveLength, double phaseOffset, int x){
        return amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset; // the -0.5 is critical
    }
    
}
