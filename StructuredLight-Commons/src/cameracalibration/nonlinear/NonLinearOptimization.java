package cameracalibration.nonlinear;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimplePointChecker;

public class NonLinearOptimization {

    public final List<RealMatrix> RT_allViews_refined;
    public final RealMatrix K_refined;
    public final List<Double> radialCoeffs_refined;
    
    public NonLinearOptimization(List<List<Double>> xyzPts, List<List<Double>> uvPts_allViews, 
                                 List<List<Double>> K_matrix, List<Double> radialCoeffs, List<List<List<Double>>> RT_allViews) {
                
        int nViews = RT_allViews.size();
        int nPts = xyzPts.size();

        // Create the builder
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        ProjectionValues projValues_norm = new ProjectionValues(xyzPts);
        ProjectionJacobian projJacob_norm = new ProjectionJacobian(xyzPts);
        builder.model(projValues_norm, projJacob_norm);
        
        // Store the rotation vectors and translation vectors
        List<Vector3D> rotVec_allViews = new ArrayList<>();
        List<Vector3D> transVec_allViews = new ArrayList<>();
        for (List<List<Double>> RT_list: RT_allViews) {
            RealMatrix RT = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(RT_list));
            
            // Rotation
            RealMatrix R = RT.getSubMatrix(0, 2, 0, 2);
            double thresh = 0.001;
            Rotation rot_mat = new Rotation(R.getData(), thresh);
            Vector3D rot_unitVec = rot_mat.getAxis(RotationConvention.FRAME_TRANSFORM);
            double rot_magnitdue = rot_mat.getAngle();
            Vector3D rot_vec = rot_unitVec.scalarMultiply(rot_magnitdue);
            rotVec_allViews.add(rot_vec);
            
            // Translation
            RealVector T = RT.getColumnVector(3);
            transVec_allViews.add(new Vector3D(T.toArray()));            
        }
        
        // Store the intrinsic matrix K and get parameters
        RealMatrix K = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(K_matrix));
        double alpha = K.getEntry(0, 0);
        double beta = K.getEntry(1, 1);
        double gamma = K.getEntry(0, 1);
        double uC = K.getEntry(0, 2);
        double vC = K.getEntry(1, 2);
        
        // Name the radial distortions
        double r0 = radialCoeffs.get(0);
        double r1 = radialCoeffs.get(1);
        
        // Build the initial guess
        double[] guess = new double[7+6*nViews];
        guess[0] = alpha;
        guess[1] = beta;
        guess[2] = gamma;
        guess[3] = uC;
        guess[4] = vC;
        guess[5] = r0;
        guess[6] = r1;
        for (int view_num = 0; view_num < nViews; view_num++) {
            int i = view_num*6 + 7;
            guess[i+0] = rotVec_allViews.get(view_num).getX();
            guess[i+1] = rotVec_allViews.get(view_num).getY();
            guess[i+2] = rotVec_allViews.get(view_num).getZ();
            guess[i+3] = transVec_allViews.get(view_num).getX();
            guess[i+4] = transVec_allViews.get(view_num).getY();
            guess[i+5] = transVec_allViews.get(view_num).getZ();
        }

        // Set the intial guess
        builder.start(guess);

        // Set the target data
        double[] uvTarget = ArrayUtils.ListToArray_Double(ArrayUtils.reshape(uvPts_allViews, 1, 2*nPts).get(0));
        builder.target(uvTarget);

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
        LeastSquaresOptimizer.Optimum solution = solver.optimize(leastSqrProb);
        
        // Report to console
        int nIters = solution.getIterations();
        System.out.println("\nSolver: Number of iterations was: " + nIters);

        // Get the solution
        RealVector X = solution.getPoint();
        
        // Extract parameters and matrices
        Parameters parameters = new Parameters(ArrayUtils.ArrayToList_Double(X.toArray()));
        K_refined = parameters.K;
        radialCoeffs_refined = parameters.radialCoeffs;
        RT_allViews_refined = parameters.RT_allViews;
    } 
}
