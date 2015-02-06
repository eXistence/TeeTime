package teetime.framework.exceptionHandling;

import teetime.framework.Stage;

public class TerminatingStageListener extends StageExceptionHandler {

	@Override
	public FurtherExecution onStageException(final Exception e, final Stage throwingStage) {
		logger.warn("Exception arised from" + throwingStage.getId(), e);
		return FurtherExecution.TERMINATE;
	}

}