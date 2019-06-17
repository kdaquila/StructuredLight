package homography.nonlinear;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<Double> xPts;
    public List<Double> yPts;
    public List<Double> uPts;
    public List<Double> vPts;
    
    public NonLinearHomography(List<List<Double>> xyPts, List<List<Double>> uvPts, 
                               List<List<Double>> h_guess) {
                
        int nPts = xyPts.size();
        
        H = MatrixUtils.createRealMatrix(3, 3);   
        
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

        // Create the builder
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        ProjectionValues projValues_norm = new ProjectionValues(xPts, yPts);
        ProjectionJacobian projJacob_norm = new ProjectionJacobian(xPts, yPts);
        builder.model(projValues_norm, projJacob_norm);

        // Set the intial guess
        List<Double> h_guess_1D = ArrayUtils.reshape(h_guess, 1, 9).get(0);
        builder.start(ArrayUtils.ListToArray_Double(h_guess_1D));

        // Set the target data
        List<Double> uvPts_1D_norm = ArrayUtils.reshape(ArrayUtils.zipLists(uPts, vPts), 1, 2*nPts).get(0);
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

        // Get the homography coefficients from the solution
        RealVector X = solution.getPoint();
        
        // Reshape into homography matrix H
        for (int row_num = 0; row_num < 3; row_num++) {
           H.setRowVector(row_num, X.getSubVector(3*row_num, 3));
        } 
               
        // Rescale H so bottom right is 1
        double H_bottomRight = H.getEntry(2, 2);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                H.setEntry(row, col, H.getEntry(row, col)/H_bottomRight);
            }
        }        
    }
    
    public List<List<Double>> getHomography() {
        return ArrayUtils.ArrayToList_Double2D(H.getData());
    }    
    
    public static Map<String,List<List<Double>>> computeHomography_batch( List<List<Double>> xyPts,  Map<String,List<List<Double>>> uvPtSets, Map<String,List<List<Double>>> homographySets) {
        Map<String,List<List<Double>>> output = new HashMap<>();
        for (String name: uvPtSets.keySet()) {
            List<List<Double>> uvPts = uvPtSets.get(name);
            List<List<Double>> homography = homographySets.get(name);
            NonLinearHomography homog = new NonLinearHomography(xyPts, uvPts, homography);
            List<List<Double>> H = homog.getHomography();
            output.put(name, H);
        }
        return output;
    }
}
