package teetime.framework.scheduling.globaltaskqueue;

import org.jctools.queues.MpmcArrayQueue;

import teetime.framework.*;
import teetime.framework.pipe.IPipe;
import teetime.framework.signal.ISignal;
import teetime.framework.signal.TerminatingSignal;

/**
 * Created by nilsziermann on 29.12.16.
 */

public class TeeTimeTaskQueueThread extends Thread {

	@Override
	public void run() {
		AbstractStage stage;
		MpmcArrayQueue<AbstractStage> taskQueue = GlobalTaskQueueScheduling.getTaskQueue();
		while (true) {
			stage = taskQueue.poll();
			AbstractStage baseStage = stage;
			if (stage != null) {
				if (stage instanceof ITaskQueueDuplicable) {
					// TODO: Implement object pool for this.
					stage = ((ITaskQueueDuplicable) stage).duplicate();
				}
				// TODO: Handle TerminateException, NotEnoughInputException
				synchronized (stage) {
					if (!(baseStage instanceof ITaskQueueDuplicable) && GlobalTaskQueueScheduling.getRunningStatefulStages().containsKey(stage)
							&& GlobalTaskQueueScheduling.getRunningStatefulStages().get(stage)) {
						GlobalTaskQueueScheduling.getTaskQueue().add(stage);
					} else if (baseStage.getCurrentState() != StageState.TERMINATED && !baseStage.shouldBeTerminated()) {
						int numOfExecutions = 1;
						StageBuffer stageBuffer = new StageBuffer(stage, false);
						if (stage instanceof ITaskQueueDuplicable) {
							numOfExecutions = drainAndReserve(baseStage, stageBuffer);
						} else if (stage instanceof ITaskQueueInformation) {
							numOfExecutions = getMaxNumberOfExecutions(baseStage);
						} else {
							// TODO: Move to function
							GlobalTaskQueueScheduling.getRunningStatefulStages().put(stage, true);
						}
						try {
							for (int i = 0; i < numOfExecutions; i++) {
								stage.executeStage();
							}
						} catch (RuntimeException e) {
							System.out.println("Stage with class: " + stage.getClass());
							throw e;
						}
						if (stage instanceof ITaskQueueDuplicable) {
							putInCorrectPipes(baseStage, stageBuffer);
						} else {
							// TODO: Move to function
							GlobalTaskQueueScheduling.getRunningStatefulStages().put(stage, false);
						}
					}
				}
				synchronized (baseStage) {
					if (baseStage.getCurrentState() != StageState.TERMINATED) {
						if (baseStage instanceof AbstractConsumerStage && baseStage.getCurrentState() != StageState.TERMINATING) {
							checkForTerminationSignal(((AbstractConsumerStage) baseStage));
						}
						if (baseStage.shouldBeTerminated()) {
							afterStageExecution(baseStage);
						}
					}
				}
			} else {
				boolean finiteProducerStagesRunning = false;
				for (AbstractStage finiteProducerStage : GlobalTaskQueueScheduling.getFiniteProducerStages()) {
					GlobalTaskQueueScheduling.getTaskQueue().add(finiteProducerStage);
					finiteProducerStagesRunning = true;
				}
				if (finiteProducerStagesRunning) {
					GlobalTaskQueueScheduling.getTaskQueue().addAll(GlobalTaskQueueScheduling.getInfiniteProducerStages());
				} else {
					break;
				}
			}
		}
		GlobalTaskQueueScheduling.getRunnableCounter().dec();
	}

	// TODO: Move to stages?
	private int drainAndReserve(final AbstractStage baseStage, final StageBuffer stageBuffer) {
		synchronized (baseStage) {
			int numberOfExecutions = getMaxNumberOfExecutions(baseStage);
			replaceAndDrainToPipes(baseStage, stageBuffer, numberOfExecutions);
			GlobalTaskQueueScheduling.getStageList().get(baseStage).add(stageBuffer);
			return numberOfExecutions;
		}
	}

	// TODO: Move to stages?
	private void replaceAndDrainToPipes(final AbstractStage baseStage, final StageBuffer stageBuffer, final int numExecutions) {
		AbstractStage stage = stageBuffer.getStage();
		synchronized (baseStage) {
			for (InputPort inputPort : stage.getInputPorts()) {
				IPipe oldPipe = inputPort.getPipe();
				IPipe newPipe = new TaskQueueBufferPipe(inputPort, null, oldPipe);
				int numElementsToDrain = numExecutions * ((ITaskQueueInformation) stage).numElementsToDrainPerExecute(inputPort);
				for (int i = 0; i < numElementsToDrain; i++) {
					Object object = oldPipe.removeLast();
					newPipe.add(object);
				}
			}
		}
		for (OutputPort outputPort : stage.getOutputPorts()) {
			IPipe oldPipe = outputPort.getPipe();
			IPipe newPipe = new TaskQueueBufferPipe(null, outputPort, oldPipe);
		}
		GlobalTaskQueueScheduling.getStageList().get(baseStage).add(stageBuffer);
	}

	// TODO: Move to stages?
	private int getMaxNumberOfExecutions(final AbstractStage stage) {
		synchronized (stage) {
			// TODO: Extract this value into Configuration or ConfigurationContext
			int maxNumExecutions = 1000;
			for (InputPort inputPort : stage.getInputPorts()) {
				int numExecutionsPort = inputPort.getPipe().size() / ((ITaskQueueInformation) stage).numElementsToDrainPerExecute(inputPort);
				maxNumExecutions = numExecutionsPort < maxNumExecutions ? numExecutionsPort : maxNumExecutions;
			}

			return maxNumExecutions;
		}
	}

	// TODO: Move to stages?
	private void putInCorrectPipes(final AbstractStage baseStage, final StageBuffer stageBuffer) {
		synchronized (baseStage) {
			// Mark element as done so it can be added to the list later
			stageBuffer.setDone(true);
			while (!GlobalTaskQueueScheduling.getStageList().get(baseStage).isEmpty() && GlobalTaskQueueScheduling.getStageList().get(baseStage).get(0).isDone()) {
				AbstractStage stage = GlobalTaskQueueScheduling.getStageList().get(baseStage).remove(0).getStage();
				for (OutputPort outputPort : stage.getOutputPorts()) {
					IPipe pipe = outputPort.getPipe();
					IPipe replacedPipe = ((TaskQueueBufferPipe) outputPort.getPipe()).getReplacedPipe();
					int n = pipe.size();
					for (int i = 0; i < n; i++) {
						Object object = pipe.removeLast();
						replacedPipe.add(object);
					}

				}
			}
		}
	}

	// TODO: Move to stages
	private void checkForTerminationSignal(final AbstractConsumerStage baseStage) {
		synchronized (baseStage) {
			if (GlobalTaskQueueScheduling.getStageList().get(baseStage).size() != 0) {
				return;
			}
			for (InputPort<?> inputPort : baseStage.getInputPorts()) {
				if (inputPort.isClosed()) {
					// stage.removeDynamicPort(inputPort);
					continue;
				} else {
					return;
				}
			}

			baseStage.terminateStage();
		}
	}

	// TODO: Move to stages
	private void afterStageExecution(final AbstractStage baseStage) {
		synchronized (baseStage) {
			if (baseStage instanceof AbstractProducerStage) {
				baseStage.onSignal(new TerminatingSignal(), null);
				GlobalTaskQueueScheduling.getFiniteProducerStages().remove(baseStage);
			} else if (baseStage instanceof AbstractConsumerStage) {
				final ISignal signal = new TerminatingSignal(); // NOPMD DU caused by loop
				for (InputPort<?> inputPort : baseStage.getInputPorts()) {
					baseStage.onSignal(signal, inputPort);
				}
			}
		}
	}
}
