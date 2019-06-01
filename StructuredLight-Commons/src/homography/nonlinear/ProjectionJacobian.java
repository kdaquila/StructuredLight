package homography.nonlinear;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class ProjectionJacobian implements MultivariateMatrixFunction {
    
    double[] X;
    double[] Y;
    int nPts;
    
    public ProjectionJacobian(List<Double> xPts, List<Double> yPts) {
        nPts = xPts.size();        
        this.X = ArrayUtils.ListToArray_Double(xPts);
        this.Y = ArrayUtils.ListToArray_Double(yPts);  
    }

    public double[][] value(double[] h) {
        
        List<List<Double>> jacobianMatrix = new ArrayList<>(nPts*2);
        for (int p = 0; p < nPts; p++) {
            
            // parameters
            double sx = h[0]*X[p] + h[1]*Y[p] + h[2];
            double sy = h[3]*X[p] + h[4]*Y[p] + h[5];
            double  w = h[6]*X[p] + h[7]*Y[p] + h[8];
            double wSqr = Math.pow(w,2);
            
            List<Double> newRow1 = new ArrayList<>(9);
            newRow1.add(X[p]/w);
            newRow1.add(Y[p]/w);
            newRow1.add(1.0/w);
            newRow1.add(0.0);
            newRow1.add(0.0);
            newRow1.add(0.0);
            newRow1.add(-sx*X[p]/wSqr);
            newRow1.add(-sx*Y[p]/wSqr);
            newRow1.add(-sx/wSqr);
            jacobianMatrix.add(newRow1);
            
            List<Double> newRow2 = new ArrayList<>(9);
            newRow2.add(0.0);
            newRow2.add(0.0);
            newRow2.add(0.0);
            newRow2.add(X[p]/w);
            newRow2.add(Y[p]/w);
            newRow2.add(1.0/w);            
            newRow2.add(-sy*X[p]/wSqr);
            newRow2.add(-sy*Y[p]/wSqr);
            newRow2.add(-sy/wSqr);
            jacobianMatrix.add(newRow2);
        }                
        
        double[][] jacobianMatrix_array2D = ArrayUtils.ListToArray_Double2D(jacobianMatrix);
              
        return jacobianMatrix_array2D;
    }
}
