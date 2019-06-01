package cameracalibration;

import core.ArrayUtils;
import core.CoordinateSystems;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class LinearHomography {
    
    private RealMatrix H;
    private List<List<Double>> imagePts;
    private List<List<Double>> worldPts;
    private int nPts;
    
    public LinearHomography(List<List<Double>> imagePts, List<List<Double>> worldPts) {
        this.imagePts = imagePts;
        this.worldPts = worldPts;
        nPts = imagePts.size();
        H = MatrixUtils.createRealMatrix(1, 1);
    }

    public void computeHomography() {
        
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
    
    public double computeReprojectError() {
        
        // Convert to homogenous coordinates
        List<List<Double>> worldPts_homog = new ArrayList<>();
        for (List<Double> pt: worldPts) {
            worldPts_homog.add(CoordinateSystems.toHomog(pt.subList(0, 2)));
        }                
        
        // Set XY points as Matrix
        RealMatrix XY_homog = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(worldPts_homog));
        RealMatrix XY_homog_tr = XY_homog.transpose();        
        
        // Do the projection
        RealMatrix UV_homog_project_tr = H.multiply(XY_homog_tr);
        RealMatrix UV_homog_project = UV_homog_project_tr.transpose();
        
        // Convert to cartesian coordinates
        List<List<Double>> imagePts_proj = new ArrayList<>();
        for (double[] pt: UV_homog_project.getData()) {
            List<Double> pt_list = ArrayUtils.ArrayToList_Double(pt);
            imagePts_proj.add(CoordinateSystems.toCartesian(pt_list));
        }  
        
        // Compute the errors
        double sumSqrDiff = 0;
        for (int i = 0; i < nPts; i++) {
            double xDiff = imagePts_proj.get(i).get(0) - imagePts.get(i).get(0);
            double yDiff = imagePts_proj.get(i).get(1) - imagePts.get(i).get(1);
            sumSqrDiff += Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
        }
        double error = sumSqrDiff/nPts;
        
        return error;
    }
}
