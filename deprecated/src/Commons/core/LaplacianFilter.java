package core;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class LaplacianFilter {
    
    public static Map<String, Map<String, Double>> computeKernalParameters_batch(Map<String, Map<String,Double>> avgWidthSets) {
        Map<String, Map<String, Double>> output = new HashMap<>();
        for (String name: avgWidthSets.keySet()) {
            Map<String, Double> avgWidths = avgWidthSets.get(name);
            Map<String, Double> kernalParams = computeKernalParameters(avgWidths);
            output.put(name, kernalParams);
        }        
        return output;
    }
    
    public static Map<String, Double> computeKernalParameters( Map<String, Double> ringWidths ) {
        double innerRingWidth = ringWidths.get("Inner");
        double outerRingWidth = ringWidths.get("Outer");
        Map<String, Double> output = new HashMap<>();
        double kernalSigma = innerRingWidth/2.0;        
        int kernalSize = (int)(outerRingWidth*1.25);
        if (kernalSize%2==0) {
            kernalSize += 1;
        }
        output.put("sigma", kernalSigma);
        output.put("size", (double) kernalSize);
        return output;
    }
    
    public static Map<String, BufferedImage> laplacianFilter_batch (Map<String, BufferedImage> grayImages, Map<String, Map<String, Double>> kernalParameterSets) {
        Map<String, BufferedImage> output = new HashMap<>();
        for (String name: grayImages.keySet()) {
            BufferedImage grayImg = grayImages.get(name);
            Map<String, Double> kernalParams = kernalParameterSets.get(name);
            BufferedImage laplacianImg = laplacianFilter(grayImg, kernalParams);
            output.put(name, laplacianImg);
        }
        return output;
    }
    
    public static BufferedImage laplacianFilter(BufferedImage grayImage, Map<String, Double> kernalParams) {
        
        double kernalSigma = kernalParams.get("sigma");
        int kernalSize = kernalParams.get("size").intValue();
        
        // Create the Laplacian filter
        float[] kernalArray = LaplacianFilter.createLaplacianKernal(kernalSigma, kernalSize);
        
        // Run the Laplacian filter
        return ImageUtils.convolve(grayImage, kernalSize, kernalArray);
    }
    
    public static float[] createLaplacianKernal(double sigma, int kernalSize) {
        
        // Check if kernal size is odd
        if ((kernalSize%2)==0) {
           throw new IllegalArgumentException("The kernal size must be a positive, odd integer.");
        }
       
        // Compute the kernal half width
        int halfWidth = (kernalSize-1)/2;
        
        
        // Fill the array
        int nElements = kernalSize*kernalSize;
        float[] output = new float[nElements];
        for (int k = 0; k<nElements; k++ ) {
            int i = k%kernalSize;
            int j = k/kernalSize;
            double x = i - halfWidth;
            double y = j - halfWidth;
            double sqrPart = (Math.pow(x,2) + Math.pow(y,2))/(2*Math.pow(sigma,2));
            double LoG = -1/(Math.PI*Math.pow(sigma,4))*(1 - sqrPart)*Math.exp(-sqrPart);
            output[k] = (float) LoG;           
        }
        
        // Compute sum
        float sum = 0;
        for (float item: output) {
            sum += item;
        }
        
        // Normalize the Array
        for (int k = 0; k<nElements; k++ ) {
            output[k] *= 1.0f/sum;
        }

        return output;
    }

}
