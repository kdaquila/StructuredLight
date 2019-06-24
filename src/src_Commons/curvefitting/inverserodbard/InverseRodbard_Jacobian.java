package curvefitting.inverserodbard;

import core.ArrayUtils;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class InverseRodbard_Jacobian implements MultivariateMatrixFunction {

    double[] xPts;
    int nPts;

    public InverseRodbard_Jacobian(double[] xPts) {
        nPts = xPts.length;
        this.xPts = xPts;
    }

    @Override
    public double[][] value(double[] parameter_array) {
        double[][] J = new double[nPts][4];
        
//        double[] p_base = parameter_array;
//        List<Double> y_base = ArrayUtils.ArrayToList_Double(InverseRodbard_Values.computeAll(parameter_array, xPts));
        
//        int nCols = parameter_array.length;
//        int nRows = nPts;  
//        RealMatrix J_Matrix = MatrixUtils.createRealMatrix(nRows, nCols);
//        double inc = 1e-10;
//        for (int col_num = 0; col_num < nCols; col_num++) {
//            // Compute high
//            double[] p_high = new double[p_base.length];
//            System.arraycopy(p_base, 0, p_high, 0, p_base.length);
//            p_high[col_num] += inc;
//            RealVector y_high = MatrixUtils.createRealVector(InverseRodbard_Values.computeAll(p_high, xPts));
//            
//            // Compute low
//            double[] p_low = new double[p_base.length];
//            System.arraycopy(p_base, 0, p_low, 0, p_base.length);
//            p_low[col_num] -= inc;
//            RealVector y_low = MatrixUtils.createRealVector(InverseRodbard_Values.computeAll(p_low, xPts));
//            
//            RealVector D = y_high.subtract(y_low).mapDivide(2*inc);
//            J_Matrix.setColumnVector(col_num, D);
//        } 
//        return J_Matrix.getData();
        
        
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            // name the parameters
            double a = parameter_array[0];
            double b = parameter_array[1];
            double c = parameter_array[2];
            double d = parameter_array[3];
            double x = xPts[pt_num];
            // compute the values
            J[pt_num][0] = Math.pow((x - b) / (c - x), 1.0 / d);
            J[pt_num][1] = a / Math.pow(c - x, 1.0 / d) * (1.0 / d) * Math.pow(x - b, 1.0 / d - 1) * -1.0;
            J[pt_num][2] = a * Math.pow(x - b, 1.0 / d) * (-1.0 / d) * Math.pow(c - x, -1.0 / d - 1.0);
            J[pt_num][3] = a * Math.log((x - b) / (c - x)) * Math.pow((x - b) / (c - x), 1.0/d) * (-1.0 / Math.pow(d, 2));
        }
        return J;
    }

}
