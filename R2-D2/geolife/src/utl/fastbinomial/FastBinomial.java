/*package utl.fastbinomial;

import java.math.BigInteger;

import utl.prime.IPrimeIteration;
import utl.prime.PositiveRange;
import utl.prime.PrimeSieve;
import utl.prime.Xint;
//http://www.luschny.de/math/factorial/FastBinomialFunction.html
public class FastBinomial {
	      
	     private FastBinomial() { }
	      
	     public static long Binomial(int n, int k) throws Exception
	     {
	         if (0 > k || k > n)
	         {
	             throw  new Exception(
	              "Binomial  0 <= k and k <= n required, but n was "
	              + n + " and k was " + k);
	         }
	      
	         if ((k == 0) || (k == n)) return 1;
	         //BigInteger bi=BigInteger.valueOf(123);
	         
	         if (k > n / 2) { k = n - k; }
	         int fi = 0, nk = n - k;
	      
	         int rootN = (int)Math.floor(Math.sqrt(n));
	         
	         int[] primes = new PrimeSieve(n).getIteration(new PositiveRange(2,n)).toArray();
	      
	         for(int prime : primes) // Equivalent to a nextPrime() function.
	         {
	             if (prime > nk)
	             {
	                 primes[fi++] = prime;
	                 continue;
	             }
	      
	             if (prime > n / 2)
	             {
	                 continue;
	             }
	      
	             if (prime > rootN)
	             {
	                 if (n % prime < k % prime)
	                 {
	                     primes[fi++] = prime;
	                 }
	                 continue;
	             }
	      
	             int r = 0, N = n, K = k, p = 1;
	      
	             while (N > 0)
	             {
	                 r = (N % prime) < (K % prime + r) ? 1 :  0;
	                 if (r == 1)
	                 {
	                     p *= prime;
	                 }
	                 N /= prime;
	                 K /= prime;
	             }
	            if (p > 1) primes[fi++] = p;
	        }
	     
	        
	         Xint p= Xint.product(primes, 0, fi);
	        return p.toLongValue();
	        }
	    

}
	     
*/