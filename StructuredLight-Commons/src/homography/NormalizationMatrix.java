package homography;

import core.ArrayUtils;
import core.CoordinateSystems;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class NormalizationMatrix {
    
    private final RealMatrix N;
    private final RealMatrix N_inv;
    
    public NormalizationMatrix(List<Double> x, List<Double> y, double factor) {        
     
        // compute the means
        double xMean = ArrayUtils.mean_Double1D(x);
        double yMean = ArrayUtils.mean_Double1D(y);
        
        // compute the standard deviations for normalization
        double xVar = ArrayUtils.var_Double1D(x);
        double yVar = ArrayUtils.var_Double1D(y);
        
        // define matrix parameters
        double alphaX = factor/ArrayUtils.avgDist(x, y, xMean, yMean);
        double alphaY = factor/ArrayUtils.avgDist(x, y, xMean, yMean);
        double betaX = -alphaX*xMean;
        double betaY = -alphaY*yMean;
        
        // Create the normalize matrix        
        N = MatrixUtils.createRealMatrix(3, 3);        
        N.setEntry(0, 0, alphaX);
        N.setEntry(0, 1, 0);
        N.setEntry(0, 2, betaX);
        N.setEntry(1, 0, 0);
        N.setEntry(1, 1, alphaY);
        N.setEntry(1, 2, betaY);
        N.setEntry(2, 0, 0);
        N.setEntry(2, 1, 0);
        N.setEntry(2, 2, 1);

        // Create the denormalize matrix 
        N_inv = MatrixUtils.createRealMatrix(3, 3);
        N_inv.setEntry(0, 0, 1/alphaX);
        N_inv.setEntry(0, 1, 0);
        N_inv.setEntry(0, 2, -betaX/alphaX);
        N_inv.setEntry(1, 0, 0);
        N_inv.setEntry(1, 1, 1/alphaY);
        N_inv.setEntry(1, 2, -betaY/alphaY);
        N_inv.setEntry(2, 0, 0);
        N_inv.setEntry(2, 1, 0);
        N_inv.setEntry(2, 2, 1);    
    }
    
    public List<List<Double>> getMatrix() {
        return ArrayUtils.ArrayToList_Double2D(N.getData());
    }
    
    public List<List<Double>> getInvMatrix() {
        return ArrayUtils.ArrayToList_Double2D(N_inv.getData());
    }

    public static void normalizePts(List<Double> x, List<Double> y, List<List<Double>> N_matrix) {

        RealMatrix N = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(N_matrix));
        
        for (int k = 0; k < x.size(); k++) {
            List<Double> xy_cart = new ArrayList<>(2);
            xy_cart.add(x.get(k));
            xy_cart.add(y.get(k));
            List<Double> xy_homog_list = CoordinateSystems.toHomog(xy_cart);
            double[] xy_homog_array = ArrayUtils.ListToArray_Double(xy_homog_list);
            RealVector XY_homog = MatrixUtils.createRealVector(xy_homog_array);
            RealVector XY_homog_norm = N.operate(XY_homog);
            List<Double> xy_homog_norm_list = ArrayUtils.ArrayToList_Double(XY_homog_norm.toArray());
            List<Double> xy_cart_norm_list = CoordinateSystems.toCartesian(xy_homog_norm_list);
            
            // overwrite the original values
            x.set(k, xy_cart_norm_list.get(0));
            y.set(k, xy_cart_norm_list.get(1));
        }  
    }          
      
}
