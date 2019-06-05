package homography.nonlinear;

import core.ArrayUtils;
import core.TXT;
import homography.HomographyError;
import homography.Normalization;
import homography.Projection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public class ProjectionValues implements MultivariateVectorFunction{
    List<Double> xPts;
    List<Double> yPts;
    List<List<Double>> xyPts;
    int nPts;
    
    public ProjectionValues(List<Double> xPts, List<Double> yPts) {
        nPts = xPts.size();        
        this.xPts = xPts;
        this.yPts = yPts;   
        xyPts = ArrayUtils.zipLists(xPts, yPts);
    }
    
    @Override
    public double[] value(double[] homographyCoeffs) {
        
        // Reformat homography
        List<Double> homographyCoeffs_list =ArrayUtils.ArrayToList_Double(homographyCoeffs);
        List<List<Double>> homographyMatrix = ArrayUtils.reshape(ArrayUtils.addDim_1Dto2D(homographyCoeffs_list), 3, 3);
        
        // Project xy points to uv space
        List<List<Double>> uvPts_proj = Projection.projectPoints(xyPts, homographyMatrix);
        
        // Reformat the projected points
        List<Double> uvPts_proj_1D = ArrayUtils.reshape(uvPts_proj, 1, nPts*2).get(0);
        double[] uvPts_proj_1D_array = ArrayUtils.ListToArray_Double(uvPts_proj_1D);
        
        return uvPts_proj_1D_array;
    }

}
