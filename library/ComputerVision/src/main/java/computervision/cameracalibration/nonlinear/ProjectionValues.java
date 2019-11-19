package cameracalibration.nonlinear;

import cameracalibration.Projection;
import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.RealMatrix;

public class ProjectionValues implements MultivariateVectorFunction{

    List<List<Double>> xyzPts;
    
    public ProjectionValues(List<List<Double>> xyzPts) {
        this.xyzPts = xyzPts;
    }

    @Override
    public double[] value(double[] parameter_array) {
        Parameters parameters = new Parameters(ArrayUtils.ArrayToList_Double(parameter_array));
        
        // Get the intrinsic matrix
        List<List<Double>> K_matrix = ArrayUtils.ArrayToList_Double2D(parameters.K.getData());
        
        // Get the radial distortion coefficients
        List<Double> radialCoeffs = parameters.radialCoeffs;
        
        // Project the XYZ points in each view using K, RT, and radials
        List<List<Double>> uvPts_allViews = new ArrayList<>();
        for (RealMatrix RT: parameters.RT_allViews) {
            // Get the extrinsic matrix
            List<List<Double>> RT_matrix = ArrayUtils.ArrayToList_Double2D(RT.getData());
            
            // Project XYZ to UV
            List<List<Double>> uvPts = Projection.projectPoints(xyzPts, K_matrix, RT_matrix, radialCoeffs);
            
            // Store the points from this view
            uvPts_allViews.addAll(uvPts);
        }
        
        // Reshape to a 1D array
        int nPts = uvPts_allViews.size();
        double[] output = ArrayUtils.ListToArray_Double(ArrayUtils.reshape(uvPts_allViews, 1, nPts*2).get(0));
        
        return output;
    }
    

}
