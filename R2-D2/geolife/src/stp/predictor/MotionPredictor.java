package stp.predictor;

import grid.RoICell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/*import mod.MovingObject;
import shape.Point;
import util.Formatter;*/

/**
 * 
 * @author su
 * 
 */
public class MotionPredictor {
	public static void main(String[] args) throws Exception {
		ArrayList<MovingObject> objlist = new ArrayList<MovingObject>();
		
		for(int i=0;i<5;i++){
			MovingObject obj=new MovingObject();
			obj.position.x=i;
			obj.position.y=i;
			obj.t=i;
			objlist.add(obj);
		}

		MotionPredictor comp = new MotionPredictor(2, 0, objlist);

		System.out.println(comp.Predict(6));
		System.out.println(comp.Predict(7));
		System.out.println(comp.Predict(8));
		System.out.println(comp.Predict(9));
		System.out.println(comp.Predict(10));
	}
	
	public static ArrayList<Point> STPGridPredictor(List<RoICell> his,int f){
		
		ArrayList<MovingObject> objlist = new ArrayList<MovingObject>();
		for(int i=0;i<5;i++){
			MovingObject obj=new MovingObject();
			obj.position.x=his.get(i).roiX;
			obj.position.y=his.get(i).roiY;
			obj.t=i;
			objlist.add(obj);
		}
		
		MotionPredictor comp = new MotionPredictor(2, 0, objlist);
		
		ArrayList<Point> res=new ArrayList<Point>();
		for(int i=5;i<f+5;i++){
			res.add(comp.Predict(i));
		}
		return res;
	}
	
	
	public static ArrayList<Point> STPLocPredictor(List<Point> his,int f){
		
		ArrayList<MovingObject> objlist = new ArrayList<MovingObject>();
		for(int i=0;i<his.size();i++){
			MovingObject obj=new MovingObject();
			obj.position.x=his.get(i).x;
			obj.position.y=his.get(i).y;
			obj.t=i;
			objlist.add(obj);
		}
		
		MotionPredictor comp = new MotionPredictor(2, 0, objlist);
		
		ArrayList<Point> res=new ArrayList<Point>();
		for(int i=5;i<f+5;i++){
			res.add(comp.Predict(i));
		}
		return res;
	}


	long reftime;
	int order;
	double[][] paraArray;

	public String toString() {
		String str = "";
		for (int i = 0; i < paraArray.length; i++) {
			for (int j = 0; j < 2; j++) {
				str += String.format("%1$-5s", Formatter.decimal_format
						.format(paraArray[i][j]))
						+ "\t";
			}
		}
		str += reftime;
		return str;
	}

	MotionPredictor(int D, long tref, ArrayList<MovingObject> objlist) {
		int h = objlist.size();
		this.order = D;
		this.reftime = tref;

		// space allocation
		paraArray = new double[order][2];

		double[][] corArray = new double[order][order];
		double[][] invCorArray = new double[order][order];
		double[][] xArray = new double[h][order];

		// x array computation
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < order; j++) {
				// if (Math.abs(objlist.get(i).t - reftime) < 0.00001)
				// xArray[i][j] = 0;
				// else
				xArray[i][j] = Math.pow((objlist.get(i).t - reftime), j);

			}
		}

		// cor array computation
		for (int i = 0; i < order; i++) {
			for (int j = 0; j < order; j++) {
				corArray[i][j] = 0;
				for (int l = 0; l < h; l++) {
					corArray[i][j] += xArray[l][i] * xArray[l][j];
				}
			}
		}

		MatrixInversion(corArray, invCorArray);

		for (int i = 0; i < order; i++) {
			double sum = 0;

			for (int j = 0; j < order; j++) {
				for (int l = 0; l < order; l++) {
					sum += corArray[i][l] * invCorArray[l][j];
				}
			}
		}

		// compute (X'X)^{-1}X'
		double[][] tempArray = new double[order][h];
		for (int i = 0; i < order; i++) {
			for (int j = 0; j < h; j++) {
				tempArray[i][j] = 0;
				for (int l = 0; l < order; l++) {
					tempArray[i][j] += invCorArray[i][l] * xArray[j][l];
				}
			}
		}

		for (int j = 0; j < order; j++) {
			paraArray[j][0] = 0;
			for (int l = 0; l < h; l++) {
				paraArray[j][0] += tempArray[j][l] * objlist.get(l).position.x;
			}
		}

		for (int j = 0; j < order; j++) {
			paraArray[j][1] = 0;
			for (int l = 0; l < h; l++) {
				paraArray[j][1] += tempArray[j][l] * objlist.get(l).position.y;
			}
		}
	}

	public Point Predict(long l) {
		int i;

		double lx = 0;

		for (i = 0; i < order; i++)
			lx += paraArray[i][0] * Math.pow((l - reftime), i);

		double ly = 0;
		for (i = 0; i < order; i++)
			ly += paraArray[i][1] * Math.pow((l - reftime), i);

		// System.out.println(l + ":" + lx + ", " + ly);

		return new Point(lx, ly);
	}

	private void MatrixInversion(double[][] A, double[][] Y) {
		double det = 1.0 / CalcDeterminant(A, order);

		// memory allocation
		double[] temp = new double[(order - 1) * (order - 1)];
		double[][] minor = new double[order - 1][order - 1];
		for (int i = 0; i < order - 1; i++) {
			for (int j = 0; j < order - 1; j++)
				minor[i][j] = temp[i * (order - 1) + j];
		}

		for (int j = 0; j < order; j++) {
			for (int i = 0; i < order; i++) {
				// get the co-factor (matrix) of A(j,i)
				GetMinor(A, minor, j, i, order);
				Y[i][j] = det * CalcDeterminant(minor, order - 1);
				if ((i + j) % 2 == 1)
					Y[i][j] = -Y[i][j];
			}
		}
	}

	private int GetMinor(double[][] src, double[][] dest, int row, int col,
			int order) {
		// indicate which col and row is being copied to dest
		int colCount = 0, rowCount = 0;

		for (int i = 0; i < order; i++) {
			if (i != row) {
				colCount = 0;
				for (int j = 0; j < order; j++) {
					// when j is not the element
					if (j != col) {
						dest[rowCount][colCount] = src[i][j];
						colCount++;
					}
				}
				rowCount++;
			}
		}

		return 1;
	}

	private double CalcDeterminant(double[][] mat, int order) {
		int i;

		// order must be >= 0
		// stop the recursion when matrix is a single element
		if (order == 1)
			return mat[0][0];

		// the determinant value
		double det = 0;

		// allocate the co-factor matrix
		double[][] minor;
		minor = new double[order - 1][order - 1];

		for (i = 0; i < order; i++) {
			// get minor of element (0,i)
			GetMinor(mat, minor, 0, i, order);
			// the recursion is here!
			det += Math.pow(-1.0, i) * mat[0][i]
					* CalcDeterminant(minor, order - 1);
		}

		return det;
	}
}
