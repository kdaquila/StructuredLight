package cameramatrix_development;

import java.util.List;

public abstract class AbstractCameraModel {
    
    public abstract void computeParameters();
    
    public abstract List<Double> project3DTo2D(List<Double> worldPt);
    
    public double computeProjectionError(List<Double> imagePtExpected, List<Double> worldPt) {
        List<Double> imagePtResult = project3DTo2D(worldPt);
        double uRes = imagePtResult.get(0);
        double vRes = imagePtResult.get(1);
        double uExp = imagePtExpected.get(0);
        double vExp = imagePtExpected.get(1);
        double error = Math.sqrt(Math.pow(uRes-uExp, 2) + Math.pow(vRes-vExp, 2));
        return error;
    }
    
    public double computeAvgProjectionError(List<List<Double>> imagePtsExpected, List<List<Double>> worldPts) {
        double sum = 0;
        int nPts = worldPts.size();
        for (int i = 0; i < nPts; i++) {
            List<Double> imagePt = imagePtsExpected.get(i);
            List<Double> worldPt = worldPts.get(i);
            sum += AbstractCameraModel.this.computeProjectionError(imagePt, worldPt);
        }
        return sum/nPts;
    }

}
