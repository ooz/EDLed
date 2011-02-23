package design.bart;

public class FFTW {
	public static class Complex {
		public double re;
		public double im;
		
		public Complex(final double re, final double im) {
			this.re = re;
			this.im = im;
		}
		public Complex(final double re) {
			this.re = re;
			this.im = 0.0;
		}
	}
}
