package curvefitting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

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
        
        // Create the matrix X
        RealMatrix M = MatrixUtils.createRealMatrix(nPts, 5);        
        for (int pt_num = 0; pt_num < nPts; pt_num++) {            
            M.setEntry(pt_num, 0, Math.pow(x[pt_num],2));
            M.setEntry(pt_num, 1, x[pt_num]*y[pt_num]);
            M.setEntry(pt_num, 2, Math.pow(y[pt_num],2));
            M.setEntry(pt_num, 3, x[pt_num]);
            M.setEntry(pt_num, 4, y[pt_num]);
        }
        
        // Create the ones matrix
        RealVector ones = MatrixUtils.createRealVector(new double[nPts]);
        for (int pt_num = 0; pt_num < nPts; pt_num++) { 
            ones.setEntry(pt_num, 1.0);
        }
        
        // Solve AX=B
        RealMatrix A = M.transpose().multiply(M);
        RealVector B = M.transpose().operate(ones);
        SingularValueDecomposition svd_A = new SingularValueDecomposition(A);
        RealVector X = svd_A.getSolver().solve(B);        
        
        // Name the parameters
        double a = X.getEntry(0);
        double b = X.getEntry(1);
        double c = X.getEntry(2);
        double d = X.getEntry(3);
        double e = X.getEntry(4);
        
        // compute theta and its sin and cos
        double theta = 0.5 * Math.atan2(b, a-c);
        double sin_th = Math.sin(theta);
        double cos_th = Math.cos(theta);
        double sin_th_sqr = Math.pow(sin_th, 2);
        double cos_th_sqr = Math.pow(cos_th, 2);
        
        // compute prime parameters
        double ap = a*cos_th_sqr + b*cos_th*sin_th + c*sin_th_sqr;
        double cp = a*sin_th_sqr - b*cos_th*sin_th + c*cos_th_sqr;
        double dp = d*cos_th + e*sin_th;
        double ep = -d*sin_th + e*cos_th;
        
        
        // Compute the ellipse center
        double x0 = -cos_th*dp/(2*ap) + sin_th*ep/(2*cp);
        double y0 = -sin_th*dp/(2*ap) - cos_th*ep/(2*cp);
        
        // store the output
        Map<String, Double> output = new HashMap<>();
        output.put("a", a);
        output.put("b", b);
        output.put("c", c);
        output.put("d", d);
        output.put("e", e);
        output.put("x0", x0);
        output.put("y0", y0);
        
        return output;
    }

}
