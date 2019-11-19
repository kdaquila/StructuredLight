package homography;

import java.util.List;

public class HomographyError {

    public static double computeReprojectError(List<List<Double>> xyPts, List<List<Double>> uvPts,  List<List<Double>> homographyMatrix) {

        int nPts = xyPts.size();

        // Project xy points to uv space
        List<List<Double>> uvPts_proj = Projection.projectPoints(xyPts, homographyMatrix);

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
