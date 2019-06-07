package cameracalibration.nonlinear;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class ProjectionJacobian implements MultivariateMatrixFunction{

    List<List<Double>> xyzPts;
    ProjectionValues projector;
    
    public ProjectionJacobian(List<List<Double>> xyzPts) {
        this.xyzPts = xyzPts;
        projector = new ProjectionValues(xyzPts);
    }

    @Override
    public double[][] value(double[] parameter_array) {
        
        double[] p_base = parameter_array;
        List<Double> uv_base = ArrayUtils.ArrayToList_Double(projector.value(parameter_array));
        
        int nCols = parameter_array.length;
        int nRows = uv_base.size();  
        RealMatrix J = MatrixUtils.createRealMatrix(nRows, nCols);
        double inc = 1e-10;
        for (int col_num = 0; col_num < nCols; col_num++) {
            // Compute high
            double[] p_high = new double[p_base.length];
            System.arraycopy(p_base, 0, p_high, 0, p_base.length);
            p_high[col_num] += inc;
            RealVector uv_high = MatrixUtils.createRealVector(projector.value(p_high));
            
            // Compute low
            double[] p_low = new double[p_base.length];
            System.arraycopy(p_base, 0, p_low, 0, p_base.length);
            p_low[col_num] -= inc;
            RealVector uv_low = MatrixUtils.createRealVector(projector.value(p_low));
            
            RealVector D = uv_high.subtract(uv_low).mapDivide(2*inc);
            J.setColumnVector(col_num, D);
        }        
        
        return J.getData();
    }

}
