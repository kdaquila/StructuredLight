package core;

import java.util.ArrayList;
import java.util.List;

public class CoordinateSystems {
    
    public static double[] toHomog(double[] cartesian) {
        
        int nPts = cartesian.length;
        
        // Make a copy
        double[] homogenous = new double[nPts+1];
        System.arraycopy(cartesian, 0, homogenous, 0, nPts);

        // Add a 1
        homogenous[nPts] = 1.0;
        
        return homogenous;
    }
    
    public static List<Double> toHomog(List<Double> cartesian) {
        
        // Make a copy
        List<Double> homogenous = new ArrayList<>(cartesian);
        
        // Add a 1
        homogenous.add(1.0);
        
        return homogenous;
    }
    
    public static double[] toCartesian(double[] homogenous) {
        int nPts = homogenous.length;
        
        // Make a copy
        double[] cartesian = new double[nPts-1];
        System.arraycopy(homogenous, 0, cartesian, 0, nPts-1);
        
        // Get the last item
        double lastItem = homogenous[nPts-1];  
        
        // Divide the remaining items by (formerly) last item
        for (int i = 0; i < nPts-1; i++) {
            cartesian[i] /= lastItem;
        }
        
        return cartesian;
    }
    
    public static List<Double> toCartesian(List<Double> homogenous) {
        // Make a copy
        List<Double> cartesian = new ArrayList<>(homogenous);
        
        // Remove and store the last item
        Double lastCoord = cartesian.remove(homogenous.size()-1);  
        
        // Divide the remaining items by (formerly) last item
        for (int i = 0; i < cartesian.size(); i++) {
            cartesian.set(i, cartesian.get(i)/lastCoord);
        }
        
        return cartesian;
    }

}
