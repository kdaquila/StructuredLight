package homography.nonlinear;

import homography.NormalizationMatrix;
import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimplePointChecker;

public class NonLinearHomography {
    
    private RealMatrix H;
    private RealMatrix H_norm;
    public final List<Double> xPts;
    public final List<Double> yPts;
    public final List<Double> uPts;
    public final List<Double> vPts;
    public final List<Double> xPts_norm;
    public final List<Double> yPts_norm;
    public final List<Double> uPts_norm;
    public final List<Double> vPts_norm;
    
    public NonLinearHomography(List<List<Double>> xyPts, List<List<Double>> uvPts, List<List<Double>> hMatrix_guess) {
                
        int nPts = xyPts.size();
        
        H = MatrixUtils.createRealMatrix(3, 3);   
        H_norm = MatrixUtils.createRealMatrix(3, 3); 
        
        // unpack the (u,v) points
        uPts = new ArrayList<>(nPts);
        vPts = new ArrayList<>(nPts);
        for (List<Double> pt: uvPts) {
            uPts.add(pt.get(0));
            vPts.add(pt.get(1));
        }
        
        // upack the (x,y) points
        xPts = new ArrayList<>(nPts);
        yPts = new ArrayList<>(nPts);    
        for (List<Double> pt: xyPts) {
            xPts.add(pt.get(0));
            yPts.add(pt.get(1));
        }
        
        // normalize (u,v) points
        NormalizationMatrix normalizeUV = new NormalizationMatrix(uPts, vPts);
        uPts_norm = new ArrayList<>(uPts);
        vPts_norm = new ArrayList<>(vPts);
        normalizeUV.normalizePts(uPts_norm, vPts_norm);  
        
        // normalize (x,y) points
        NormalizationMatrix normalizeXY = new NormalizationMatrix(xPts, yPts);
        xPts_norm = new ArrayList<>(xPts);
        yPts_norm = new ArrayList<>(yPts);
        normalizeXY.normalizePts(xPts_norm, yPts_norm); 

        // Create the builder
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        ProjectionValues projValues_norm = new ProjectionValues(xPts_norm, yPts_norm);
        ProjectionJacobian projJacob_norm = new ProjectionJacobian(xPts_norm, yPts_norm);
        builder.model(projValues_norm, projJacob_norm);

        // Set the intial guess
        List<Double> h_guess_1D = ArrayUtils.reshape(hMatrix_guess, 1, 9).get(0);
        builder.start(ArrayUtils.ListToArray_Double(h_guess_1D));

        // Set the target data
        List<Double> uvPts_1D_norm = ArrayUtils.reshape(ArrayUtils.zipLists(uPts_norm, vPts_norm), 1, 2*nPts).get(0);
        builder.target(ArrayUtils.ListToArray_Double(uvPts_1D_norm));

        // Set the options
        double relThresh = 0.01;
        double absThresh = 1e-6;    
        builder.checkerPair(new SimplePointChecker<>(relThresh, absThresh));
        int maxEval = 10000;
        builder.maxEvaluations(maxEval);
        int maxIter = 100;
        builder.maxIterations(maxIter);        

        // Set unused features
        builder.lazyEvaluation(false);
        builder.parameterValidator(null);
        builder.weight(null);

        // Solve the least squares problem using the Levenberg-Marquadt optimizer
        LevenbergMarquardtOptimizer solver = new LevenbergMarquardtOptimizer();
        LeastSquaresProblem leastSqrProb = builder.build();
        Optimum solution = solver.optimize(leastSqrProb);
        
        // Report to console
        int nIters = solution.getIterations();
        System.out.println("\nSolver: Number of iterations was: " + nIters);

        // Get the homography coefficients from the solution
        RealVector X = solution.getPoint();
        
        // Reshape into homography matrix H
        for (int row_num = 0; row_num < 3; row_num++) {
           H_norm.setRowVector(row_num, X.getSubVector(3*row_num, 3));
        } 
        
        // Denormalize H
        double[][] UV_denormalize_data = ArrayUtils.ListToArray_Double2D(normalizeUV.getInvMatrix());
        RealMatrix UV_denormalize = MatrixUtils.createRealMatrix(UV_denormalize_data);
        
        double[][] XY_normalize_data = ArrayUtils.ListToArray_Double2D(normalizeXY.getMatrix());
        RealMatrix XY_normalize = MatrixUtils.createRealMatrix(XY_normalize_data);
        
        H = UV_denormalize.multiply(H_norm.multiply(XY_normalize)); 
        
        // Rescale H so bottom right is 1
        double H_bottomRight = H.getEntry(2, 2);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                H.setEntry(row, col, H.getEntry(row, col)/H_bottomRight);
            }
        }
        
        // Rescale H_norm so bottom right is 1
        double H_norm_bottomRight = H_norm.getEntry(2, 2);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                H_norm.setEntry(row, col, H_norm.getEntry(row, col)/H_norm_bottomRight);
            }
        }
    }
    
    public List<List<Double>> getHomography() {
        return ArrayUtils.ArrayToList_Double2D(H.getData());
    }
    
    public List<List<Double>> getNormalizedHomography() {
        return ArrayUtils.ArrayToList_Double2D(H_norm.getData());
    }

}
