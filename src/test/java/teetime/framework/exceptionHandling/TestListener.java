package teetime.framework.exceptionHandling;

import teetime.framework.Stage;

public class TestListener extends AbstractExceptionListener {

	public static int exceptionInvoked = 0;

	public TestListener() {
		TestListener.exceptionInvoked = 0;
	}

	@Override
	public FurtherExecution onStageException(final Exception e, final Stage throwingStage) {
		exceptionInvoked++;
		if (exceptionInvoked == 2) {
			return FurtherExecution.TERMINATE;
		} else {
			return FurtherExecution.CONTINUE;
		}
	}

}
