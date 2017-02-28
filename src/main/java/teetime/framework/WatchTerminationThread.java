package teetime.framework;

import java.util.*;

class WatchTerminationThread extends Thread {

	private final List<AbstractStage> consumerStages;

	private volatile boolean shouldTerminate;

	public WatchTerminationThread() {
		consumerStages = Collections.synchronizedList(new ArrayList<>());
		setDaemon(true);
	}

	@Override
	public void run() {
		while (!shouldTerminate) {
			synchronized (consumerStages) {
				Iterator<AbstractStage> iterator = consumerStages.iterator();
				while (iterator.hasNext()) {
					AbstractStage stage = iterator.next();
					// FIXME remove <; so far, we use it for d&c
					if (stage.getNumOpenedInputPorts().get() <= 0 && stage.getCurrentState() == StageState.STARTED) {
						stage.terminateStage();
						stage.logger.debug("Terminated stage: " + stage);
						iterator.remove();
					}
				}
			}

			// if (consumerStages.isEmpty()) {
			// shouldTerminate = true;
			// break;
			// }

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				shouldTerminate = true;
			}
		}

	}

	public void addConsumerStage(final AbstractStage stage) {
		// only add consumers
		if (stage.getTerminationStrategy() == TerminationStrategy.BY_SIGNAL) {
			consumerStages.add(stage);
		}
	}

	// public void isShouldTerminate() {
	// shouldTerminate = true;
	// }
}
