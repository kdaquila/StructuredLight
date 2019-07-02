package curvefitting.inverserodbard;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public class InverseRodbard_Values implements MultivariateVectorFunction {

    double[] xPts;
    int nPts;

    public InverseRodbard_Values(double[] xPts) {
        nPts = xPts.length;
        this.xPts = xPts;
    }

    @Override
    public double[] value(double[] parameters) {
        // compute the values
        double[] output = new double[nPts];
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            double x = xPts[pt_num];
            output[pt_num] = compute(parameters, x);
        }
        return output;
    }
    
    public static double[] computeAll(double[] parameters, double[] xArray) {
        double[] output = new double[xArray.length];
        for (int i = 0; i < xArray.length; i++) {
            output[i] = InverseRodbard_Values.compute(parameters, xArray[i]);
        }
        return output;
    }

    public static double compute(double[] parameters, double x) {
        double a = parameters[0];
        double b = parameters[1];
        double c = parameters[2];
        double d = parameters[3];
        double base = (x - b) / (c - x);
        double I = 0;
        if (base < 0) {
            throw new RuntimeException("The Inverse Rodbard function value is non-real. a,b,c,d: " + a + ", " + b + ", " + c + ", " + d);
        } 
        I = a*Math.pow(base, 1 / d);
        return I;
    }

}
