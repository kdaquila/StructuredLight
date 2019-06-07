package cameracalibration.nonlinear;

import java.util.List;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;

public class ProjectionJacobian implements MultivariateMatrixFunction{

    public ProjectionJacobian(List<List<Double>> xyzPts) {
    }

    @Override
    public double[][] value(double[] point) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
