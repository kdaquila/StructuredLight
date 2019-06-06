package cameracalibration;

import java.util.List;

public class ProjectionError {
    
    public static double computeReprojectError(List<List<Double>> xyzPts, List<List<Double>> uvPts, List<List<Double>> K,  List<List<Double>> RT) {

        int nPts = xyzPts.size();

        // Project xy points to uv space
        List<List<Double>> uvPts_proj = cameracalibration.Projection.projectPoints(xyzPts, K, RT);

        // Compute the errors
        double sumSqrDiff = 0;
        for (int i = 0; i < nPts; i++) {
            double xDiff = uvPts_proj.get(i).get(0) - uvPts.get(i).get(0);
            double yDiff = uvPts_proj.get(i).get(1) - uvPts.get(i).get(1);
            sumSqrDiff += Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
        }
        double error = sumSqrDiff/nPts;

        return error;
    }

}
