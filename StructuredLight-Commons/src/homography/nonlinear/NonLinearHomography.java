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
    
    public NonLinearHomography() {
        H = MatrixUtils.createRealMatrix(3, 3);
    }

    public void computeHomography(List<List<Double>> xyPts, List<List<Double>> uvPts,
                                  List<List<Double>> hMatrix_guess) {
        int nPts = xyPts.size();

        // Create the builder
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Define x, y, u, v points
        List<Double> xPts = new ArrayList<>(nPts);
        List<Double> yPts = new ArrayList<>(nPts);   
        List<Double> uPts = new ArrayList<>(nPts);
        List<Double> vPts = new ArrayList<>(nPts);
        for (int i = 0; i < nPts; i++) {
            xPts.add(xyPts.get(i).get(0));
            yPts.add(xyPts.get(i).get(1));
            uPts.add(uvPts.get(i).get(0));
            vPts.add(uvPts.get(i).get(1));
        }
        
        // normalize (x,y) points
        NormalizationMatrix normalizeXY = new NormalizationMatrix(xPts, yPts);
        normalizeXY.normalizePts(xPts, yPts);  
        
        // normalize (u,v) points
        NormalizationMatrix normalizeUV = new NormalizationMatrix(uPts, vPts);
        normalizeUV.normalizePts(uPts, vPts);   

        // Set the model
        ProjectionValues projValues = new ProjectionValues(xPts, yPts);
        ProjectionJacobian projJacob = new ProjectionJacobian(xPts, yPts);
        builder.model(projValues, projJacob);

        // Set the intial guess
        List<Double> h_guess_1D = ArrayUtils.reshape(hMatrix_guess, 1, 9).get(0);
        builder.start(ArrayUtils.ListToArray_Double(h_guess_1D));

        // Set the target data
        List<Double> uvPts_1D = ArrayUtils.reshape(ArrayUtils.zipLists(uPts, vPts), 1, 2*nPts).get(0);
        builder.target(ArrayUtils.ListToArray_Double(uvPts_1D));

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
        RealVector h_norm = solution.getPoint();
        
        // Reshape into homography matrix H
        for (int row_num = 0; row_num < 3; row_num++) {
           H.setRowVector(row_num, h_norm.getSubVector(3*row_num, 3));
        } 
        
        // Denormalize H
        double[][] UV_denormalize_data = ArrayUtils.ListToArray_Double2D(normalizeUV.getInvMatrix());
        RealMatrix UV_denormalize = MatrixUtils.createRealMatrix(UV_denormalize_data);
        
        double[][] XY_normalize_data = ArrayUtils.ListToArray_Double2D(normalizeXY.getMatrix());
        RealMatrix XY_normalize = MatrixUtils.createRealMatrix(XY_normalize_data);
        
        H = UV_denormalize.multiply(H.multiply(XY_normalize)); 
    }
    
    public List<List<Double>> getHomography() {
        return ArrayUtils.ArrayToList_Double2D(H.getData());
    }

}
