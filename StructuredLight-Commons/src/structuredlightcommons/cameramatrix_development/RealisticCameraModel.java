package cameracalibration;

import structuredlightcommons.ArrayUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimplePointChecker;

public class RealisticCameraModel extends AbstractCameraModel {
    
    List<List<Double>> imagePts;
    List<List<Double>> worldPts;
    double[][] projectionGuess;
    double[] distortionsGuess;
    double[][] projectionFinal;
    double[] distortionsFinal;
    int totalIterations;
    
    public RealisticCameraModel(List<List<Double>> imagePts, List<List<Double>> worldPts, double[][] projectionGuess, double[] distortionGuess) {
        this.imagePts = imagePts;
        this.worldPts = worldPts;
        this.projectionGuess = projectionGuess;
        this.distortionsGuess = distortionGuess;
    }
    
    @Override
    public void computeParameters() {
        
        // Create a least squares problem
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        RealisticCameraProjection model = new RealisticCameraProjection(worldPts);
        builder.model(model);
        
        // Set the intial guess
        double[] initialParams = new double[15];
        for (int guessIndex = 0; guessIndex < 11; guessIndex++) {
            int row = guessIndex/4;
            int col = guessIndex - row*4;
            initialParams[guessIndex] = projectionGuess[row][col];            
        }
        for (int row = 0, guessIndex = 11; row < 4; row++, guessIndex++) {
            initialParams[guessIndex] = distortionsGuess[row];
        }
        builder.start(initialParams);
        
        // Set the target data
        int nPts = imagePts.size();
        double[] uvPts = new double[nPts*2];
        int targetIndex = 0;
        for (List<Double> pt: imagePts) {
            for (Double coord: pt) {
                uvPts[targetIndex] = coord;
                targetIndex++;
            }
        }
        builder.target(uvPts);
        
        // Set the options
        double relThresh = 1e-18;
        double absThresh = 1e-18;             
        builder.checkerPair(new SimplePointChecker<>(relThresh, absThresh));
        int maxEval = 10000;
        builder.maxEvaluations(maxEval);
        int maxIter = 10000;
        builder.maxIterations(maxIter);
        builder.lazyEvaluation(false);
        
        // Set unused features
        builder.parameterValidator(null);
        builder.weight(null);
        
        // Solve the least squares problem using the Levenberg-Marquadt optimizer
        LevenbergMarquardtOptimizer solver = new LevenbergMarquardtOptimizer();
        LeastSquaresOptimizer.Optimum solution = solver.optimize(builder.build());
        
        // Organize the results
        RealVector projectionVector = solution.getPoint();
        projectionFinal = new double[][]{{0d,0d,0d,0d},{0d,0d,0d,0d},{0d,0d,0d,1d}};
        for (int i = 0; i < 11; i++) {
            int row = i/4;
            int col = i - row*4;
            projectionFinal[row][col] = projectionVector.getEntry(i);
        }
        distortionsFinal = new double[4];
        for (int i = 0; i < 4; i++) {
            distortionsFinal[i] = projectionVector.getEntry(i+11);
        }        
        totalIterations = solution.getIterations();
        
    }
    
    @Override
    public List<Double> project3DTo2D(List<Double> xyz) {
        List<Double> imagePt = new ArrayList<>();
        
        double[] M = ArrayUtils.flattenArrayOfArrays(projectionFinal);
        double x = xyz.get(0);
        double y = xyz.get(1);
        double z = xyz.get(2);
        double s = (x*M[0] + y*M[1] + z*M[2] + M[3]) / (x*M[8] + y*M[9] + z*M[10] + 1d );
        double t = (x*M[4] + y*M[5] + z*M[6] + M[7]) / (x*M[8] + y*M[9] + z*M[10] + 1d );
        double[] d = distortionsFinal;        
        double u = (s + d[0]*s*pow(t,2) + d[0]*pow(s,3) + d[1]*pow(s,5) + d[1]*s*pow(t,4) + 2*d[1]*pow(s,3)*pow(t,2) + 2*d[2]*s*t + 3*d[3]*pow(s,2) + d[3]*pow(t,2) );
        double v = (t + d[0]*t*pow(s,2) + d[0]*pow(t,3) + d[1]*pow(t,5) + d[1]*t*pow(s,4) + 2*d[1]*pow(t,3)*pow(s,2) + 2*d[3]*s*t + 3*d[2]*pow(t,2) + d[2]*pow(s,2) );
        List<Double> uv = Arrays.asList(new Double[]{u, v});
        return uv;
    }

    private double pow(double a, double b) {
        return Math.pow(a, b);
    }
    
   

}
