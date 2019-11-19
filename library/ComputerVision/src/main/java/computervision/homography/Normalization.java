package homography;

import core.ArrayUtils;
import core.CoordinateSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Normalization {
    
    private final RealMatrix N_xy;
    private final RealMatrix N_xy_inv;
    private final RealMatrix N_uv;
    private final RealMatrix N_uv_inv;
    private final double normScaleFactor;
    
    public Normalization(List<List<Double>> xy, List<List<Double>> uv) {    
        
        normScaleFactor = 0.07;
        
        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();
        ArrayUtils.unzipList(xy, x, y);
        List<Double> u = new ArrayList<>();
        List<Double> v = new ArrayList<>();
        ArrayUtils.unzipList(uv, u, v);
     
        // compute the xy means
        double xMean = ArrayUtils.mean_Double1D(x);
        double yMean = ArrayUtils.mean_Double1D(y);
                
        // define xy matrix parameters
        double alphaX = normScaleFactor/ArrayUtils.avgDist(x, y, xMean, yMean);
        double alphaY = normScaleFactor/ArrayUtils.avgDist(x, y, xMean, yMean);
        double betaX = -alphaX*xMean;
        double betaY = -alphaY*yMean;
        
        // Create the normalize matrix        
        N_xy = MatrixUtils.createRealMatrix(3, 3);        
        N_xy.setEntry(0, 0, alphaX);
        N_xy.setEntry(0, 1, 0);
        N_xy.setEntry(0, 2, betaX);
        N_xy.setEntry(1, 0, 0);
        N_xy.setEntry(1, 1, alphaY);
        N_xy.setEntry(1, 2, betaY);
        N_xy.setEntry(2, 0, 0);
        N_xy.setEntry(2, 1, 0);
        N_xy.setEntry(2, 2, 1);

        // Create the denormalize matrix 
        N_xy_inv = MatrixUtils.createRealMatrix(3, 3);
        N_xy_inv.setEntry(0, 0, 1/alphaX);
        N_xy_inv.setEntry(0, 1, 0);
        N_xy_inv.setEntry(0, 2, -betaX/alphaX);
        N_xy_inv.setEntry(1, 0, 0);
        N_xy_inv.setEntry(1, 1, 1/alphaY);
        N_xy_inv.setEntry(1, 2, -betaY/alphaY);
        N_xy_inv.setEntry(2, 0, 0);
        N_xy_inv.setEntry(2, 1, 0);
        N_xy_inv.setEntry(2, 2, 1); 
        
        // compute the uv means
        double uMean = ArrayUtils.mean_Double1D(u);
        double vMean = ArrayUtils.mean_Double1D(v);
                
        // define xy matrix parameters
        double alphaU = normScaleFactor/ArrayUtils.avgDist(u, v, uMean, vMean);
        double alphaV = normScaleFactor/ArrayUtils.avgDist(u, v, uMean, vMean);
        double betaU = -alphaU*uMean;
        double betaV= -alphaV*vMean;
        
        // Create the normalize matrix    
        N_uv = MatrixUtils.createRealMatrix(3, 3);        
        N_uv.setEntry(0, 0, alphaU);
        N_uv.setEntry(0, 1, 0);
        N_uv.setEntry(0, 2, betaU);
        N_uv.setEntry(1, 0, 0);
        N_uv.setEntry(1, 1, alphaV);
        N_uv.setEntry(1, 2, betaV);
        N_uv.setEntry(2, 0, 0);
        N_uv.setEntry(2, 1, 0);
        N_uv.setEntry(2, 2, 1);

        // Create the denormalize matrix 
        N_uv_inv = MatrixUtils.createRealMatrix(3, 3);
        N_uv_inv.setEntry(0, 0, 1/alphaU);
        N_uv_inv.setEntry(0, 1, 0);
        N_uv_inv.setEntry(0, 2, -betaU/alphaU);
        N_uv_inv.setEntry(1, 0, 0);
        N_uv_inv.setEntry(1, 1, 1/alphaV);
        N_uv_inv.setEntry(1, 2, -betaV/alphaV);
        N_uv_inv.setEntry(2, 0, 0);
        N_uv_inv.setEntry(2, 1, 0);
        N_uv_inv.setEntry(2, 2, 1); 
    }
        
    public List<List<Double>> normalizePointsXY(List<List<Double>> xy_cart_list) {              
        return normalizePoints(xy_cart_list, N_xy);
    }
    
    public Map<String,List<List<Double>>> normalizePointsUV_batch(Map<String,List<List<Double>>> uvPtSets) {
        Map<String,List<List<Double>>> output = new HashMap<>();
        for (String name: uvPtSets.keySet()) {
            List<List<Double>> uvPts = uvPtSets.get(name);
            List<List<Double>> uvPts_norm = normalizePointsUV(uvPts);
            output.put(name, uvPts_norm);
        }
        return output;
    }
    
    public List<List<Double>> normalizePointsUV(List<List<Double>> uv_cart_list) {              
        return normalizePoints(uv_cart_list, N_uv);
    } 
    
    private List<List<Double>> normalizePoints(List<List<Double>> xy_cart_list, RealMatrix N) {        
        int nPts = xy_cart_list.size();                
        List<List<Double>> xy_cart_list_norm = new ArrayList<>(nPts);
        for (List<Double> xy_cart: xy_cart_list) {
            RealVector xy_homog = MatrixUtils.createRealVector(ArrayUtils.ListToArray_Double(CoordinateSystems.toHomog(xy_cart)));
            RealVector xy_homog_norm = N.operate(xy_homog);
            List<Double> xy_cart_norm = CoordinateSystems.toCartesian(ArrayUtils.ArrayToList_Double(xy_homog_norm.toArray()));
            xy_cart_list_norm.add(xy_cart_norm);
        }          
        return xy_cart_list_norm;
    }  
    
    public Map<String,List<List<Double>>> denormalizeHomographyMatrix_batch(Map<String,List<List<Double>>> H_norm_matrix) {
        Map<String,List<List<Double>>> output = new HashMap<>();
        for (String name: H_norm_matrix.keySet()) {
            List<List<Double>> H_norm = H_norm_matrix.get(name);
            List<List<Double>> H = denormalizeHomographyMatrix(H_norm);
            output.put(name, H);
        }
        return output;
    }

    public List<List<Double>> denormalizeHomographyMatrix(List<List<Double>> H_norm_matrix) {
        RealMatrix H_norm = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(H_norm_matrix));
        RealMatrix H = N_uv_inv.multiply(H_norm.multiply(N_xy));
        return ArrayUtils.ArrayToList_Double2D(H.getData());
    }
    
    public List<List<Double>> denormalizeSymmetricMatrix(List<List<Double>> B_norm_matrix) {
        RealMatrix B_norm = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(B_norm_matrix));
        RealMatrix B = N_uv.transpose().multiply(B_norm.multiply(N_uv));
        return ArrayUtils.ArrayToList_Double2D(B.getData());
    }
      
}
