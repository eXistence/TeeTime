package teetime.stage.basic.distributor;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.jctools.queues.QueueFactory;
import org.jctools.queues.spec.ConcurrentQueueSpec;
import org.jctools.queues.spec.Ordering;
import org.jctools.queues.spec.Preference;

import teetime.framework.AbstractStage;
import teetime.framework.InputPort;
import teetime.framework.OutputPort;
import teetime.framework.Stage;
import teetime.framework.exceptionHandling.AbstractExceptionListener.FurtherExecution;
import teetime.framework.exceptionHandling.StageException;
import teetime.stage.basic.distributor.DynamicPortActionContainer.DynamicPortAction;
import teetime.util.concurrent.queue.PCBlockingQueue;
import teetime.util.concurrent.queue.putstrategy.PutStrategy;
import teetime.util.concurrent.queue.putstrategy.YieldPutStrategy;
import teetime.util.concurrent.queue.takestrategy.SCParkTakeStrategy;
import teetime.util.concurrent.queue.takestrategy.TakeStrategy;

public class ControlledDistributor<T> extends AbstractStage {

	// private final InputPort<DynamicPortActionContainer<T>> dynamicPortActionInputPort = createInputPort();
	private final InputPort<T> inputPort = createInputPort();

	private final OutputPort<T> outputPort = createOutputPort();

	private final BlockingQueue<DynamicPortActionContainer<T>> actions;

	public ControlledDistributor() {
		final Queue<DynamicPortActionContainer<T>> localQueue = QueueFactory.newQueue(new ConcurrentQueueSpec(1, 1, 0, Ordering.FIFO, Preference.THROUGHPUT));
		final PutStrategy<DynamicPortActionContainer<T>> putStrategy = new YieldPutStrategy<DynamicPortActionContainer<T>>();
		final TakeStrategy<DynamicPortActionContainer<T>> takeStrategy = new SCParkTakeStrategy<DynamicPortActionContainer<T>>();
		actions = new PCBlockingQueue<DynamicPortActionContainer<T>>(localQueue, putStrategy, takeStrategy);
	}

	@Override
	public void onStarting() throws Exception {
		getDistributor(outputPort); // throws an ClassCastException if it is not a distributor
		super.onStarting();
	}

	@Override
	// first, receive exact one element from the inputPort
	// second, receive exact one element from the dynamicPortActionInputPort
	// next, repeat in this order
	protected void executeStage() {
		T element = inputPort.receive();
		if (null == element) {
			returnNoElement();
		}
		passToDistributor(element);

		try {
			// DynamicPortActionContainer<T> dynamicPortAction = dynamicPortActionInputPort.receive();
			DynamicPortActionContainer<T> dynamicPortAction = actions.take();
			// DynamicPortActionContainer<T> dynamicPortAction = actions.poll();
			if (null == dynamicPortAction) {
				returnNoElement();
			}
			checkForOutputPortChange(dynamicPortAction);

		} catch (InterruptedException e) {
			final FurtherExecution furtherExecution = exceptionHandler.onStageException(e, this);
			if (furtherExecution == FurtherExecution.TERMINATE) {
				throw new StageException(e, this);
			}
		}
	}

	private void checkForOutputPortChange(final DynamicPortActionContainer<T> dynamicPortAction) {
		System.out.println("" + dynamicPortAction.getDynamicPortAction());

		switch (dynamicPortAction.getDynamicPortAction()) {
		case CREATE:
			Distributor<T> distributor = getDistributor(outputPort);
			OutputPort<T> newOutputPort = distributor.getNewOutputPort();
			dynamicPortAction.execute(newOutputPort);
			break;
		case REMOVE:
			// TODO implement "remove port at runtime"
			break;
		default:
			if (logger.isWarnEnabled()) {
				logger.warn("Unhandled switch case of " + DynamicPortAction.class.getName() + ": " + dynamicPortAction.getDynamicPortAction());
			}
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private Distributor<T> getDistributor(final OutputPort<T> outputPort2) {
		final Stage owningStage = outputPort.getPipe().getTargetPort().getOwningStage();
		return (Distributor<T>) owningStage;
	}

	private void passToDistributor(final T element) {
		System.out.println("Passing " + element);
		outputPort.send(element);
	}

	public InputPort<T> getInputPort() {
		return inputPort;
	}

	// public InputPort<DynamicPortActionContainer<T>> getDynamicPortActionInputPort() {
	// return dynamicPortActionInputPort;
	// }

	public OutputPort<T> getOutputPort() {
		return outputPort;
	}

	public Queue<DynamicPortActionContainer<T>> getActions() {
		return actions;
	}

}
