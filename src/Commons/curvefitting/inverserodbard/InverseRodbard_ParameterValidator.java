package curvefitting.inverserodbard;

import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

public class InverseRodbard_ParameterValidator implements ParameterValidator{
    
    public RealVector validate (RealVector input) {
        RealVector output = MatrixUtils.createRealVector(input.toArray());
        
        double a = output.getEntry(0);
        double b = output.getEntry(1);
        double c = output.getEntry(2);
        double d = output.getEntry(3);
        
        if ( a < 50.0) {
            output.setEntry(0, 50.0);
        }
        
        if ( b > 0) {
            output.setEntry(1, 0.0);
        }
        
        if ( c <= 65536.0) {
            output.setEntry(2, 65536.0 + 1e-10);
        }
        
        if ( d <= 0.0) {
            output.setEntry(3, 0.1);
        }

        return output;
    }
}
