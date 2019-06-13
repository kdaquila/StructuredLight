package cameracalibration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public static double computeReprojectError(List<List<Double>> xyzPts, List<List<Double>> uvPts, List<List<Double>> K,  List<List<Double>> RT, List<Double> radialDistCoeffs) {

        int nPts = xyzPts.size();

        // Project xy points to uv space
        List<List<Double>> uvPts_proj = cameracalibration.Projection.projectPoints(xyzPts, K, RT, radialDistCoeffs);

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
    
    public static Map<String,Double> computeReprojectError_batch(List<List<Double>> xyzPts, List<List<Double>> K, List<Double> radialDistCoeffs, Map<String,List<List<Double>>> uvPtSet,  Map<String,List<List<Double>>> RTMatrixSet) {
        Map<String,Double> errors = new HashMap<>();
        for (String name: RTMatrixSet.keySet()) {
            List<List<Double>> RT = RTMatrixSet.get(name);
            List<List<Double>> uvpts = uvPtSet.get(name);
            double error = computeReprojectError(xyzPts, uvpts, K, RT, radialDistCoeffs);
            errors.put(name,error);
        }
        return errors;
    } 

}
