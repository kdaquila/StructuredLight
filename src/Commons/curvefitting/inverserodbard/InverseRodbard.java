package curvefitting.inverserodbard;

import core.ArrayUtils;
import core.Print;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimplePointChecker;

public class InverseRodbard {
    
    public int nPts;
    public double[] xArray;
    public double[] yArray;
    public double[] params;
    
    public InverseRodbard(double[] x, double[]y) {
        nPts = x.length;
        this.xArray = x;
        this.yArray = y;
        params = new double[4];
    }
     
    public Map<String,Double> fit() {
                        
        // Create the builder
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        InverseRodbard_Values functionValues = new InverseRodbard_Values(xArray);
        InverseRodbard_Jacobian jacobianValues = new InverseRodbard_Jacobian(xArray);
        builder.model(functionValues, jacobianValues);        
        
        // Set the intial guess
        double[] guess = new double[4];
        guess[0] = 65535.0; //a
        guess[1] = 0.0; //b
        guess[2] = 65535.0; //c
        guess[3] = 3.0; //d
        builder.start(guess);                

        // Set the target data
        builder.target(yArray);

        // Set the options
        double relThresh = 0.01;
        double absThresh = 1e-10;    
//        builder.checker(new EvaluationRmsChecker(0.001));
        builder.checkerPair(new SimplePointChecker<>(relThresh, absThresh));
        int maxEval = 10000;
        builder.maxEvaluations(maxEval);
        int maxIter = 10000;
        builder.maxIterations(maxIter);        

        // Set special features
        builder.lazyEvaluation(false);
//        builder.parameterValidator(null);
        builder.parameterValidator(new InverseRodbard_ParameterValidator());
        builder.weight(null);   
                
        // Create the least squares problem
        LeastSquaresProblem leastSqrProb = builder.build();      

        // Solve the least squares problem using the Levenberg-Marquadt optimizer
        LevenbergMarquardtOptimizer solver = new LevenbergMarquardtOptimizer();
        
        LeastSquaresOptimizer.Optimum solution = solver.optimize(leastSqrProb);        

        // Get the parameters from the solution
        params = solution.getPoint().toArray();
        
        // Compute the values
        double[] y_comp = functionValues.value(params);
        RealVector y_comp_vec = MatrixUtils.createRealVector(y_comp);
        RealVector y_vec = MatrixUtils.createRealVector(yArray);
        RealVector errorVec = y_comp_vec.subtract(y_vec); 
        double residualSumSqr = Math.pow(errorVec.getNorm(), 2);
        double yVar = ArrayUtils.var_Double1D(ArrayUtils.ArrayToList_Double(yArray)); 
        double residualVar = residualSumSqr/nPts;
        double rms = Math.sqrt(residualVar);
        double rSqr = 1 - residualVar/yVar;
          
        if (rSqr < 0.90){
            Print.println("warning low fit rSqr: " + rSqr);
        }
        
        Print.println("fit rms: " + solution.getRMS());
        Print.println("fit rSqr: " + rSqr);
        Print.println("fit iterations: " + solution.getIterations());
        Print.println("fit params: " + ArrayUtils.ArrayToList_Double(params));
        
        // store the output
        Map<String, Double> output = new HashMap<>();
        output.put("a", params[0]);
        output.put("b", params[1]);
        output.put("c", params[2]);
        output.put("d", params[3]);
                
        return output;
        
    }
}


