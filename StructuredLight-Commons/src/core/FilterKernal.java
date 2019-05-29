package core;

public class FilterKernal {
    
    public static float[] Laplacian(double sigma, int kernalSize) {
        
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
