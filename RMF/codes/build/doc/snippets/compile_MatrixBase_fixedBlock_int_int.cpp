#include <Eigen/Core>
#include <Eigen/LU>
#include <Eigen/QR>
#include <Eigen/Cholesky>
#include <Eigen/Geometry>
#include <Eigen/Jacobi>
#include <Eigen/Eigenvalues>
#include <iostream>

using namespace Eigen;
using namespace std;

int main(int, char**)
{
  cout.precision(3);
  Matrix4d m = Vector4d(1,2,3,4).asDiagonal();
cout << "Here is the matrix m:" << endl << m << endl;
cout << "Here is m.fixed<2, 2>(2, 2):" << endl << m.block<2, 2>(2, 2) << endl;
m.block<2, 2>(2, 0) = m.block<2, 2>(2, 2);
cout << "Now the matrix m is:" << endl << m << endl;

  return 0;
}
