package curvefitting;

import core.ArrayUtils;
import core.Print;
import core.TXT;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.Map;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import static curvefitting.Values.compute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

public class Gaussian2D {
    
    BufferedImage inputImg;
    
    public Gaussian2D(BufferedImage grayImg) {
        this.inputImg = grayImg;        
    }
     
    public Map<String,Double> fit(int centerX, int centerY, int w, int h) {
        
        // Compute the top left point
        int x = centerX - (w-1)/2;
        int y = centerY - (h-1)/2;        
       
        // Get the data from region of interest
        Raster regionRaster = inputImg.getData(new Rectangle(x, y, w, h));
        byte[] regionData = ((DataBufferByte) regionRaster.getDataBuffer()).getData();
        
        // create x, y, and I value lists
        int nPts = w*h;
        double[] xArray = new double[nPts];
        double[] yArray = new double[nPts];
        double[] iArray = new double[nPts];        
        for (int k = 0; k < nPts; k++) {
            int col = k%w;
            int row = k/w;
            int i = col + x;
            int j = row + y;
            xArray[k] = i;
            yArray[k] = j;
            iArray[k] = regionData[k] & 0xFF;            
        }
                
        // Create the builder
        LeastSquaresBuilder builder = new LeastSquaresBuilder();
        
        // Set the model
        Values gaussianValues = new Values(xArray, yArray);
        Jacobian gaussianJacobian = new Jacobian(xArray, yArray);
        builder.model(gaussianValues, gaussianJacobian);

        // Set the intial guess
        double iMean = ArrayUtils.mean_Double1D(ArrayUtils.ArrayToList_Double(iArray));
        double[] guess = new double[7];
        guess[0] = -200; //a
        guess[1] = centerX; //b
        guess[2] = 0.00045; //c
        guess[3] = centerY; //d
        guess[4] = 0.0015; //e
        guess[5] = 0.0001; //f
        guess[6] = 400; //g
        builder.start(guess);

        // Set the target data
        builder.target(iArray);

        // Set the options
        double relThresh = 0.01;
        double absThresh = 1e-6;    
        builder.checker(new EvaluationRmsChecker(0.001));
//        builder.checkerPair(new SimplePointChecker<>(relThresh, absThresh));
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
        

        // Get the parameters from the solution
        double[] params = solution.getPoint().toArray();
        
        // Compute the values
        Values values = new Values(xArray, yArray);
        double[] I_comp = values.value(params);
        RealVector I_comp_vec = MatrixUtils.createRealVector(I_comp);
        RealVector I_vec = MatrixUtils.createRealVector(iArray);
        double iVar = ArrayUtils.var_Double1D(ArrayUtils.ArrayToList_Double(iArray));
        RealVector errorVec = I_comp_vec.subtract(I_vec);     
        double residualSumSqr = Math.pow(errorVec.getNorm(), 2);
        double residualVar = residualSumSqr/(w*h);
        double rms = Math.sqrt(residualVar);
        double rSqr = 1 - residualVar/iVar;
        
//        String debugPath1 = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\Debug\\iDataActual.txt";
//        List<Double> iList = ArrayUtils.ArrayToList_Double(iArray);
//        List<List<Double>> iMatrix = new ArrayList<>();
//        iMatrix.add(iList);
//        iMatrix = ArrayUtils.reshape(iMatrix, 15, 15);
//        TXT.saveMatrix(iMatrix, Double.class, debugPath1, "%.2f");
//        
//        String debugPath2 = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\Debug\\iDataComp.txt";
//        List<Double> iList2 = ArrayUtils.ArrayToList_Double(I_comp);
//        List<List<Double>> iMatrix2 = new ArrayList<>();
//        iMatrix2.add(iList2);
//        iMatrix2 = ArrayUtils.reshape(iMatrix2, 15, 15);
//        TXT.saveMatrix(iMatrix2, Double.class, debugPath2, "%.2f");
//        
        if (rSqr < 0.90){
            Print.println("warning low gaussian fit rSqr: " + rSqr);
        }
//        
//        Print.println("gaussian fit rms: " + solution.getRMS());
//        Print.println("gaussian fit iterations: " + solution.getIterations());
//        Print.println("gaussian fit params: " + ArrayUtils.ArrayToList_Double(params));
        
        // store the output
        Map<String, Double> output = new HashMap<>();
        output.put("a", params[0]);
        output.put("b", params[1]);
        output.put("c", params[2]);
        output.put("d", params[3]);
        output.put("e", params[4]);
        output.put("x0", params[1]);
        output.put("y0", params[3]);
        
        return output;
        
    }

}

class Values implements MultivariateVectorFunction{
    double[] xPts;
    double[] yPts;
    int nPts;
    
    public Values(double[] xPts, double[] yPts) {
        nPts = xPts.length;        
        this.xPts = xPts;
        this.yPts = yPts;
    }
    
    @Override
    public double[] value(double[] parameters) {
        
        // compute the values
        double[] output = new double[nPts];
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            double x = xPts[pt_num];
            double y = yPts[pt_num];
            output[pt_num] = compute(parameters, x, y);
        }
        return output;
    }
    
    public static double compute(double[] parameters, double x, double y) {
        double a = parameters[0];
        double b = parameters[1];
        double c = parameters[2];
        double d = parameters[3];
        double e = parameters[4];
        double f = parameters[5];
        double g = parameters[6];
        double I = a*Math.exp(-(c*Math.pow(x-b,2) + 2*f*(x-b)*(y-d)+ e*Math.pow(y-d,2))) + g;
        return I;
    }
}

class Jacobian implements MultivariateMatrixFunction {
    
    double[] xPts;
    double[] yPts;
    int nPts;
    
    public Jacobian(double[] xPts, double[] yPts) {
        nPts = xPts.length;        
        this.xPts = xPts;
        this.yPts = yPts;
    }

    @Override
    public double[][] value(double[] parameters) {
        
        double[][] J = new double[nPts][7];
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            
            // name the parameters
            double a = parameters[0];
            double b = parameters[1];
            double c = parameters[2];
            double d = parameters[3];
            double e = parameters[4];
            double f = parameters[5];
            
            double x = xPts[pt_num];
            double y = yPts[pt_num];
            
            // compute the values            
            J[pt_num][0] = Values.compute(parameters, x, y);
            J[pt_num][1] = a*Values.compute(parameters, x, y)*(2*c*(x-b) + 2*f*(y-d));
            J[pt_num][2] = a*Values.compute(parameters, x, y)*-Math.pow(x-b,2);
            J[pt_num][3] = a*Values.compute(parameters, x, y)*(2*e*(y-d) + 2*f*(x-b));
            J[pt_num][4] = a*Values.compute(parameters, x, y)*-Math.pow(y-d,2);
            J[pt_num][5] = a*Values.compute(parameters, x, y)*2*(x-b)*(y-d);
            J[pt_num][6] = 1.0;
        }                
                      
        return J;
    }
}
