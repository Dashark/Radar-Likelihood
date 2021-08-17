package myexamples;
import Jama.*; 
import java.util.Date;

/** Example of use of Matrix Class, featuring magic squares. **/

public class MagicSquareExample1 {

   /** Generate magic square test matrix. **/

   public static Matrix magic (int n) {

      double[][] M = new double[n][n];

      // Odd order

      if ((n % 2) == 1) {
         int a = (n+1)/2;
         int b = (n+1);
         for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
               M[i][j] = n*((i+j+a) % n) + ((i+2*j+b) % n) + 1;
            }
         }

      // Doubly Even Order

      } else if ((n % 4) == 0) {
         for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
               if (((i+1)/2)%2 == ((j+1)/2)%2) {
                  M[i][j] = n*n-n*i-j;
               } else {
                  M[i][j] = n*i+j+1;
               }
            }
         }

      // Singly Even Order

      } else {
         int p = n/2;
         int k = (n-2)/4;
         Matrix A = magic(p);
         for (int j = 0; j < p; j++) {
            for (int i = 0; i < p; i++) {
               double aij = A.get(i,j);
               M[i][j] = aij;
               M[i][j+p] = aij + 2*p*p;
               M[i+p][j] = aij + 3*p*p;
               M[i+p][j+p] = aij + p*p;
            }
         }
         for (int i = 0; i < p; i++) {
            for (int j = 0; j < k; j++) {
               double t = M[i][j]; M[i][j] = M[i+p][j]; M[i+p][j] = t;
            }
            for (int j = n-k+1; j < n; j++) {
               double t = M[i][j]; M[i][j] = M[i+p][j]; M[i+p][j] = t;
            }
         }
         double t = M[k][0]; M[k][0] = M[k+p][0]; M[k+p][0] = t;
         t = M[k][k]; M[k][k] = M[k+p][k]; M[k+p][k] = t;
      }
      return new Matrix(M);
   }

   /** Shorten spelling of print. **/

   private static void print (String s) {
      System.out.print(s);
   }
   
   /** Format double with Fw.d. **/

   public static String fixedWidthDoubletoString (double x, int w, int d) {
      java.text.DecimalFormat fmt = new java.text.DecimalFormat();
      fmt.setMaximumFractionDigits(d);
      fmt.setMinimumFractionDigits(d);
      fmt.setGroupingUsed(false);
      String s = fmt.format(x);
      while (s.length() < w) {
         s = " " + s;
      }
      return s;
   }

   /** Format integer with Iw. **/

   public static String fixedWidthIntegertoString (int n, int w) {
      String s = Integer.toString(n);
      while (s.length() < w) {
         s = " " + s;
      }
      return s;
   }

	private static final int Dimension = 3; 
	private static Matrix _sigma_coefficient = new Matrix(Dimension, Dimension, 0.01);
	private static Matrix _sigma_inv;
	private static double _sigma_det, _sigma_prev;

	public static void CorrelationProbabilityServiceImpl() {
		_sigma_coefficient.set(0, 0, 0.005);
      _sigma_coefficient.set(0, 1, 0.1);
		_sigma_coefficient.set(1, 1, 0.005);
		_sigma_coefficient.set(2, 2, 0.005);
      print("Covariance : \n");
      _sigma_coefficient.print(Dimension, Dimension);

		_sigma_det = Math.sqrt(_sigma_coefficient.det());
      print("Covariance Determinant Squart :\n");
      print(fixedWidthDoubletoString(_sigma_det, 12, 13));
      print("\n");
		_sigma_inv = _sigma_coefficient.inverse();
      print("Covariance Inverse :\n");
      _sigma_inv.print(Dimension, Dimension);
      print("\n");
		_sigma_prev = Math.pow(2 * Math.PI, Dimension / 2.0);
      print("2 * PI (K/2) :\n");
      print(fixedWidthDoubletoString(_sigma_prev, 12, 3));
      print("\n");
	}

	public static double[] calcuateProbability(double[] cond_array, double[] tarobj_array) {
		Matrix cond = new Matrix(cond_array, 1); 
      cond.print(3,3);
		Matrix target_obj = new Matrix(tarobj_array, 3);
      target_obj.print(3,3);
		assert (target_obj.getRowDimension() == 3); 
		// a set of target to only one object
		Matrix results = new Matrix(1, target_obj.getColumnDimension());

		int cols = target_obj.getColumnDimension();
		for (int i = 0; i < cols; ++i) {
			Matrix temp = target_obj.getMatrix(0, Dimension - 1, i, i);
         temp.print(3,3);
			Matrix temp_trans = temp.transpose(); 
			Matrix temp1 = temp_trans.times(_sigma_inv); 
			Matrix temp2 = temp1.times(temp);
         temp2.print(3,3);
			double lamda = Math.exp(-temp2.get(0, 0) / 2.0);
         print(fixedWidthDoubletoString(lamda, 12, 13));
         print("\n");
			results.set(0, i, lamda / _sigma_prev / _sigma_det); // prob density of all
															// targets to one
															// object
		}
		results.arrayTimesEquals(cond);
		cols = results.getColumnDimension();
		double[][] results_array = results.getArrayCopy();
		double result_sum = 0.0;
		for (int i = 0; i < cols; ++i) {
			result_sum += results_array[0][i];
		}
		for (int i = 0; i < cols; ++i) {
			results_array[0][i] = results_array[0][i] / result_sum;
		}
		return results_array[0];
	}


   public static void main (String argv[]) {

   /* 
    | Tests LU, QR, SVD and symmetric Eig decompositions.
    |
    |   n       = order of magic square.
    |   trace   = diagonal sum, should be the magic sum, (n^3 + n)/2.
    |   max_eig = maximum eigenvalue of (A + A')/2, should equal trace.
    |   rank    = linear algebraic rank,
    |             should equal n if n is odd, be less than n if n is even.
    |   cond    = L_2 condition number, ratio of singular values.
    |   lu_res  = test of LU factorization, norm1(L*U-A(p,:))/(n*eps).
    |   qr_res  = test of QR factorization, norm1(Q*R-A)/(n*eps).
    */

      print("\n    Test of Matrix Class, using magic squares.\n");
      print("    See MagicSquareExample.main() for an explanation.\n");
      print("\n      n     trace       max_eig   rank        cond      lu_res      qr_res\n\n");

      CorrelationProbabilityServiceImpl();
      Date start_time = new Date();
      double eps = Math.pow(2.0,-52.0);
      for (int n = 3; n <= 3; n++) {
         //print(fixedWidthIntegertoString(n,7));

         // Matrix M = magic(n);
         Matrix M = new Matrix(3, 3, 0.01); 
         M.set(0, 0, 0.005);
         M.set(0, 1, 0.1);
         M.set(1, 1, 0.005);
         M.set(2, 2, 0.005);
         Matrix M1 = M.transpose();
         //print("Covariance Transpose :\n");
         //M1.print(n, n);
         Matrix M2 = M.inverse();
         /*
         Matrix object = Matrix.random(3,1);
         object.print(3, 1);
         Matrix to = target.minus(object);
         to.print(3, 1);
         Matrix to_t = to.transpose();
         print("\n");
         print(fixedWidthDoubletoString(M2.normInf(), 12, 3));
         Matrix sub = M.getMatrix(0, 0, 0, 2);
         sub.print(3,3);
         print(fixedWidthDoubletoString(sub.normInf(), 12, 3));
         Matrix to_times = to_t.times(M2);
         Matrix to_times1 = to_times.times(to);
         to_times.print(3,3);
         to_times1.print(1,3);
         Matrix arraytimes = to_times.arrayTimes(sub);
         arraytimes.print(3,3);
         print(fixedWidthDoubletoString(M.det(),12,3));
         print("\n");
         print(fixedWidthDoubletoString(M1.det(),12,3));
         */
         Matrix cond = Matrix.random(1,3);
         print("Conditional Probability :\n");
         cond.print(3, 3);
         double[][] Marray = cond.getArray();
         Matrix coord = Matrix.random(3,3);
         print("x - Oj :\n");
         coord.print(3, 3);
         double[] Mone = coord.getColumnPackedCopy();
         double[] res = calcuateProbability(Marray[0], Mone);
         Matrix Mres = new Matrix(res, 1);
         print("Result :\n");
         Mres.print(3,3);
      }
      Date stop_time = new Date();
      double etime = (stop_time.getTime() - start_time.getTime())/1000.;
      print("\nElapsed Time = " + 
         fixedWidthDoubletoString(etime,12,3) + " seconds\n");
      print("Adios\n");
   }
}
