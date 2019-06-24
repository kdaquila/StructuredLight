package curvefitting.inverserodbard;

import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

public class InverseRodbard_ParameterValidator implements ParameterValidator{
    
    public RealVector validate (RealVector input) {
        RealVector output = MatrixUtils.createRealVector(input.toArray());
        for (int i = 0; i < input.getDimension(); i++) {
            double entry = output.getEntry(i);
            double entryAbs = Math.abs(entry);
            if (entryAbs > 500.0) {
                entryAbs = 500.0;
            }
            output.setEntry(i, entryAbs);
            
        }
        return output;
    }
}
