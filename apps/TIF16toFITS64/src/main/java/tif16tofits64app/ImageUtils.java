package tif16tofits64app;

public class ImageUtils {

    public static double[][][] multiplyByScalar3D(double[][][] inputArray, double factor) {
        int nSlices = inputArray.length;
        int nRows = inputArray[0].length;
        int nCols = inputArray[0][0].length;
        double[][][] outputArray = new double[nSlices][nRows][nCols];
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            outputArray[slice_num] = multiplyByScalar2D(inputArray[slice_num], factor);
        }
        return outputArray;
    }

    public static double[][] multiplyByScalar2D(double[][] inputArray, double factor) {

        int nRows = inputArray.length;
        int nCols = inputArray[0].length;
        double[][] outputArray = new double[nRows][nCols];
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                outputArray[row_num][col_num] = inputArray[row_num][col_num]*factor;
            }
        }
        return outputArray;
    }
}
