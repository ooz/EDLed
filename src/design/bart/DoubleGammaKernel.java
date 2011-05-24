package design.bart;

public class DoubleGammaKernel extends DesignKernel {
	
	public static class GammaParams {
		/* Time unit: ms */
		public static final int    TPEAK1_DEFAULT  = 5400;
		public static final int    MWIDTH1_DEFAULT = 2500;
		public static final double SCALE1_DEFAULT  = 1.0;
		public static final int    TPEAK2_DEFAULT  = 8800;
		public static final int    MWIDTH2_DEFAULT = 3350;
		public static final double SCALE2_DEFAULT  = 0.25;
		public static final int    OFFSET_DEFAULT  = 0;
		
		public static final int    SPM_TPEAK1_DEFAULT  = 6000;
		public static final int    SPM_MWIDTH1_DEFAULT = 5100;
		public static final double SPM_SCALE1_DEFAULT  = 1.0;
		public static final int    SPM_TPEAK2_DEFAULT  = 16000;
		public static final int    SPM_MWIDTH2_DEFAULT = 8000;
		public static final double SPM_SCALE2_DEFAULT  = 0.09;
		public static final int    SPM_OFFSET_DEFAULT  = 0;
		
		/** Time to response-peak. */
		public int tPeak1;
		/** Dispersion of response. */
		public int mWidth1;
		/** Scale of response. */
		public double scale1;
		/** Time to undershoot-valley. */
		public int tPeak2;
		/** Dispersion of undershoot. */
		public int mWidth2;
		/** Scale of undershoot. */
		public double scale2;
		/** Offset of the function. */
		public int offset;
		public int overallWidth;
		
		/* Three constants used in the double gamma function and its derivatives. */
		private static final double C1 = 2;
		private static final double C2 = 8.0;
		private static final double C3 = Math.log(2);
		
		public GammaParams() {
			
		}
	}

	@Override
	public float[][] plotGammaWithDerivs(int derivs) {
		// TODO Auto-generated method stub
		return null;
	}

}
