package teetime.examples.experiment14;

import static org.junit.Assert.assertEquals;
import teetime.examples.HostName;

class ChwHomePerformanceCheck extends AbstractPerformanceCheck {

	@Override
	public String getCorrespondingPerformanceProfile() {
		return HostName.CHW_HOME.toString();
	}

	@Override
	public void check() {
		super.check();

		double medianSpeedup = (double) test14.quantiles.get(0.5) / test01.quantiles.get(0.5);

		System.out.println("medianSpeedup (14): " + medianSpeedup);

		// until 25.06.2014 (incl.)
		// assertEquals(60, (double) test14.quantiles.get(0.5) / test1.quantiles.get(0.5), 5.1);
		// since 26.06.2014 (incl.)
		// assertEquals(76, medianSpeedup, 5.1); // +16
		// since 04.07.2014 (incl.)
		// assertEquals(86, medianSpeedup, 5.1); // +16
		// since 11.08.2014 (incl.)
		// assertEquals(103, medianSpeedup, 5.1); // +17
		// since 31.08.2014 (incl.)
		// assertEquals(62, medianSpeedup, 2.1); // -41
		// since 04.11.2014 (incl.)
		// assertEquals(84, medianSpeedup, 2.1); // +22
		// since 05.12.2014 (incl.)
		// assertEquals(75, medianSpeedup, 2.1); // -9
		// since 13.12.2014 (incl.)
		// assertEquals(44, medianSpeedup, 2.1); // -31
		// since 28.12.2014 (incl.)
		assertEquals(46, medianSpeedup, 2.1); // +2
	}
}
