package core;

import java.util.ArrayList;
import java.util.List;

public class CoordinateSystems {
    
    public static List<Double> toHomog(List<Double> cartesian) {
        
        // Make a copy
        List<Double> homogenous = new ArrayList<>(cartesian);
        
        // Add a 1
        homogenous.add(1.0);
        
        return homogenous;
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
