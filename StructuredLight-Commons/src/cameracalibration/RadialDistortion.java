package cameracalibration;

import core.ArrayUtils;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class RadialDistortion {

    public static List<Double> compute(List<List<Double>> intrinsicMatrix, List<List<Double>> observedPts, List<List<Double>> projectedPts) {
        
        int nPts = observedPts.size();
        
        // Build Matrix A
        RealMatrix A = MatrixUtils.createRealMatrix(2*nPts, 2);
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            int row_num = 2*pt_num;
            double u = projectedPts.get(pt_num).get(0);
            double v = projectedPts.get(pt_num).get(1);
            double rSqr = Math.pow(u, 2) + Math.pow(v, 2);            
            A.setEntry(row_num, 0, u*rSqr);
            A.setEntry(row_num, 1, u*Math.pow(rSqr, 2));
            A.setEntry(row_num+1, 0, v*rSqr);
            A.setEntry(row_num+1, 1, v*Math.pow(rSqr, 2));
        }
        
        
        // Build Vector B
        RealVector B = MatrixUtils.createRealVector(new double[2*nPts]);
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            int row_num = 2*pt_num;
            B.setEntry(row_num, observedPts.get(pt_num).get(0) - projectedPts.get(pt_num).get(0));            
            B.setEntry(row_num+1, observedPts.get(pt_num).get(1) - projectedPts.get(pt_num).get(1));
        }
        
        // Compute Solution Vector X
        SingularValueDecomposition decomp = new SingularValueDecomposition(A);
        RealVector X = decomp.getSolver().solve(B);
        
        // Report to console        
//        System.out.println("The condition number for radial distortion matrix is : " + String.format("%.3e", decomp.getConditionNumber()));
        
        return ArrayUtils.ArrayToList_Double(X.toArray());
    }
}
