package cameracalibration;

import structuredlightcommons.ArrayUtils;
import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optim.SimplePointChecker;

public class CameraLensModel extends AbstractCameraModel {
    
    int nPts;
    double[][] initialImagePts;
    double[][] finalImagePts;
    double[][] actualImagePts;
    double[] initialDistortions;
    double[] finalDistortions;
    int totalIterations;
    
    public CameraLensModel (double[][] initialImagePts, double[][] actualImagePts, double[] distortionsGuess) {
        this.initialImagePts = initialImagePts;
        this.actualImagePts = actualImagePts;
        this.initialDistortions = distortionsGuess;
        nPts = initialImagePts.length;
    }

    @Override
    public void computeParameters() {
        // Create a least squares problem
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        CameraLensDistortion model = new CameraLensDistortion(initialImagePts);
        builder.model(model);
        
        // Set the intial guess
        builder.start(initialDistortions);
        
        // Set the target data
        double[] actualImagePtsFlat = ArrayUtils.flattenArrayOfPairs(actualImagePts);
        builder.target(actualImagePtsFlat);
        
        // Set the options
        double relThresh = 1e-12;
        double absThresh = 1e-12;             
        builder.checker(new EvaluationRmsChecker(relThresh, absThresh));
        int maxEval = 10000;
        builder.maxEvaluations(maxEval);
        int maxIter = 10000;
        builder.maxIterations(maxIter);
        builder.lazyEvaluation(false);
        
        // Set unused features
        builder.parameterValidator(null);
        builder.weight(null);
        
        // Solve the least squares problem using the Levenberg-Marquadt optimizer
//        LevenbergMarquardtOptimizer solver = new LevenbergMarquardtOptimizer();
        GaussNewtonOptimizer solver = new GaussNewtonOptimizer();
        LeastSquaresOptimizer.Optimum solution = solver.optimize(builder.build());
        
        // Organize the results
        finalDistortions = solution.getPoint().toArray();
        finalImagePts = ArrayUtils.unflattenArrayToPairs(model.value(new ArrayRealVector(finalDistortions)).getFirst().toArray());
        totalIterations = solution.getIterations();
    }

    @Override
    public List<Double> project3DTo2D(List<Double> worldPt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
