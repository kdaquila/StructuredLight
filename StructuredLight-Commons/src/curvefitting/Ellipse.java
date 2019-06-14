package curvefitting;

import core.ArrayUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Ellipse {    
    
    
    
    public static Map<String,Double> fit(List<List<Integer>> contour) {
        
        int nPts = contour.size();
        
        // create x, y arrays
        double[] x = new double[nPts];
        double[] y = new double[nPts];       
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            x[pt_num] = contour.get(pt_num).get(0);
            y[pt_num] = contour.get(pt_num).get(1);         
        }
        
        // Create the matrix M
        RealMatrix M = MatrixUtils.createRealMatrix(6, 6);        
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            
            // Create the transformed point vector
            RealMatrix U = MatrixUtils.createRealMatrix(6, 1);
            U.setEntry(0, 0, Math.pow(x[pt_num],2));
            U.setEntry(1, 0, Math.pow(y[pt_num],2));
            U.setEntry(2, 0, x[pt_num]*y[pt_num]);
            U.setEntry(3, 0, x[pt_num]);
            U.setEntry(4, 0, y[pt_num]);
            U.setEntry(5, 0, 1);
            
            // Create new addition to M
            RealMatrix M_i = U.multiply(U.transpose());
            
            // Add it to M
            M = M.add(M_i);
        }
        
        // Compute the parameters
        EigenDecomposition decomp = new EigenDecomposition(M);
        List<Double> eigenValues = ArrayUtils.ArrayToList_Double(decomp.getRealEigenvalues());
        int minIndex = eigenValues.indexOf(Collections.min(eigenValues));
        double[] p = decomp.getEigenvector(minIndex).toArray();
        
        // Name the parameters
        double a = p[0];
        double b = p[1];
        double c = p[2];
        double d = p[3];
        double e = p[4];
        double f = p[5];
        
        // Compute the ellipse center
        double x0 = (2*b*d - e*c)/(Math.pow(c,2) - 4*a*b);
        double y0 = (2*a*e - d*c)/(Math.pow(c,2) - 4*a*b);
        
        // store the output
        Map<String, Double> output = new HashMap<>();
        output.put("a", a);
        output.put("b", b);
        output.put("c", c);
        output.put("d", d);
        output.put("e", e);
        output.put("f", f);
        output.put("x0", x0);
        output.put("y0", y0);
        
        return output;
    }

}
