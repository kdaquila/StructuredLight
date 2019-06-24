package calibrationpattern.rings;

import java.util.ArrayList;
import java.util.List;

public class WorldRings {

    public static List<List<Double>> computeCenters(int nRows, int nCols, double dx, double dy) {
        List<List<Double>> centers = new ArrayList<>();
        for (int rowInd = 0; rowInd < nRows; rowInd++) {
            for (int colInd = 0; colInd < nCols; colInd++) {
                List<Double> newPt = new ArrayList<>(3);
                Double x = colInd*dx;
                Double y = rowInd*dy;
                Double z = 0.0;
                newPt.add(x);
                newPt.add(y);
                newPt.add(z);
                centers.add(newPt);
            }
        }
        return centers;
    }
}
