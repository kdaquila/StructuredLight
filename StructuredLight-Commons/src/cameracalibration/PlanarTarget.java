package cameracalibration;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class PlanarTarget {
    
    private RealMatrix H;

    public void computeHomography(List<List<Double>> imagePts, List<List<Double>> worldPts) {
        
        int nPts = imagePts.size();
        
        // unpack the (u,v) points
        List<Double> uPts = new ArrayList<>(nPts);
        List<Double> vPts = new ArrayList<>(nPts);
        for (List<Double> pt: imagePts) {
            uPts.add(pt.get(0));
            vPts.add(pt.get(1));
        }
        
        // normalize (u,v) points
        NormalizationMatrix normalizeUV = new NormalizationMatrix(uPts, vPts);
        normalizeUV.normalizePts(uPts, vPts);        
        
        // upack the (x,y) points
        List<Double> xPts = new ArrayList<>(nPts);
        List<Double> yPts = new ArrayList<>(nPts);
        for (List<Double> pt: worldPts) {
            xPts.add(pt.get(0));
            yPts.add(pt.get(1));
        }
        
        // normalize (x,y) points
        NormalizationMatrix normalizeXY = new NormalizationMatrix(xPts, yPts);
        normalizeXY.normalizePts(xPts, yPts);         
        
        // build matrix A and vector B
        RealVector B = new ArrayRealVector(2*nPts);
        RealMatrix A = new Array2DRowRealMatrix(2*nPts, 9);
        for (int i = 0; i < nPts; i++) {
            
           
            Double ui = uPts.get(i);
            Double vi = vPts.get(i);
            Double xi = xPts.get(i);
            Double yi = yPts.get(i);
            
            // set vector B elements
            B.setEntry(i, 0);
            B.setEntry(i+nPts, 0);           
            
            // set matrix A elements
            A.setEntry(i, 0, -xi);
            A.setEntry(i, 1, -yi);
            A.setEntry(i, 2, -1);
            A.setEntry(i, 3, 0);
            A.setEntry(i, 4, 0);
            A.setEntry(i, 5, 0);
            A.setEntry(i, 6, ui*xi);
            A.setEntry(i, 7, ui*yi);
            A.setEntry(i, 8, ui);
            A.setEntry(i+nPts, 0, 0);
            A.setEntry(i+nPts, 1, 0);
            A.setEntry(i+nPts, 2, 0);
            A.setEntry(i+nPts, 3, -xi);
            A.setEntry(i+nPts, 4, -yi);
            A.setEntry(i+nPts, 5, -1);
            A.setEntry(i+nPts, 6, vi*xi);
            A.setEntry(i+nPts, 7, vi*yi);
            A.setEntry(i+nPts, 8, vi);
        }

        // TODO debug remove
        ArrayUtils.printList_Double2D(ArrayUtils.ArrayToList_Double2D(A.getData()));
        
        // Solve for Homography Coefficients
        SingularValueDecomposition decomp = new SingularValueDecomposition(A);
        System.out.println("The condition number is : " + String.format("%.3e", decomp.getConditionNumber()));
        RealMatrix V = decomp.getV();
        RealVector h_norm = V.getColumnVector(V.getColumnDimension() - 1);

        // Reshape into homography matrix H
        RealMatrix H_norm = MatrixUtils.createRealMatrix(3, 3);
        for (int row_num = 0; row_num < 3; row_num++) {
           H_norm.setRowVector(row_num, h_norm.getSubVector(3*row_num, 3));
        } 
        
        // Denormalize
        double[][] UV_denormalize_data = ArrayUtils.ListToArray_Double2D(normalizeUV.getInvMatrix());
        RealMatrix UV_denormalize = MatrixUtils.createRealMatrix(UV_denormalize_data);
        
        double[][] XY_normalize_data = ArrayUtils.ListToArray_Double2D(normalizeXY.getMatrix());
        RealMatrix XY_normalize = MatrixUtils.createRealMatrix(XY_normalize_data);
        
        H = UV_denormalize.multiply(H_norm.multiply(XY_normalize));        
                
    }
    
    public List<List<Double>> getHomography() {
        return ArrayUtils.ArrayToList_Double2D(H.getData());
    }
}
