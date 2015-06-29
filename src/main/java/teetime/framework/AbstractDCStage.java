package teetime.framework;

import teetime.framework.signal.InitializingSignal;
import teetime.framework.signal.StartingSignal;

/**
 * Represents a stage to provide functionality for the divide and conquer paradigm
 *
 * @since 2.x
 *
 * @author Christian Wulf, Nelson Tavares de Sousa, Robin Mohr
 *
 */
public abstract class AbstractDCStage<I> extends AbstractStage {

	private final ConfigurationContext context;

	protected final InputPort<I> inputPort = this.createInputPort();
	protected final InputPort<I> leftInputPort = this.createInputPort();
	protected final InputPort<I> rightInputPort = this.createInputPort();

	protected final OutputPort<I> outputPort = this.createOutputPort();
	protected final OutputPort<I> leftOutputPort = this.createOutputPort();
	protected final OutputPort<I> rightOutputPort = this.createOutputPort();

	/**
	 * Divide and Conquer stages need the configuration context upon creation
	 *
	 */
	public AbstractDCStage(final ConfigurationContext context) {
		if (null == context) {
			throw new IllegalArgumentException("Context may not be null.");
		}
		this.context = context;
		// connect to self instead of dummy pipe upon creation
		context.connectPorts(leftOutputPort, leftInputPort, 1);
		context.connectPorts(rightOutputPort, rightInputPort, 1);
	}

	public final InputPort<I> getInputPort() {
		return this.inputPort;
	}

	public final InputPort<I> getLeftInputPort() {
		return this.leftInputPort;
	}

	public final InputPort<I> getRightInputPort() {
		return this.rightInputPort;
	}

	public final OutputPort<I> getOutputPort() {
		return this.outputPort;
	}

	public final OutputPort<I> getleftOutputPort() {
		return this.leftOutputPort;
	}

	public final OutputPort<I> getrightOutputPort() {
		return this.rightOutputPort;
	}

	@Override
	protected final void executeStage() {
		final I element = this.getInputPort().receive();
		final I eLeft = this.getLeftInputPort().receive();
		final I eRight = this.getRightInputPort().receive();

		if (null != element) {
			if (splitCondition(element)) {

				// divide the input
				divide(element);

				// create two new instances of this stage
				createCopies();

				// send results from split
				leftOutputPort.send(eLeft);
				rightOutputPort.send(eRight);

				// send signals init start
				leftOutputPort.sendSignal(new InitializingSignal());
				leftOutputPort.sendSignal(new StartingSignal());
				rightOutputPort.sendSignal(new InitializingSignal());
				rightOutputPort.sendSignal(new StartingSignal());
			}
		} else if (eLeft != null && eRight != null) {

			outputPort.send(element);
		} else {
			returnNoElement();
		}
	}

	/**
	 * A method to add two copies (new instances) of this stage to the configuration, which should be executed in a own thread.
	 *
	 */
	@SuppressWarnings("unchecked")
	protected void createCopies() {
		try {
			// this creates an instance of the subclass

			// FIXME temporary solution
			final AbstractDCStage<I> newStage1 = this.getClass().newInstance();
			final AbstractDCStage<I> newStage2 = this.getClass().newInstance();

			// connect with copies
			context.connectPorts(leftOutputPort, newStage1.getInputPort(), 1);
			context.connectPorts(newStage1.getOutputPort(), leftInputPort, 1);

			context.connectPorts(rightOutputPort, newStage2.getInputPort(), 1);
			context.connectPorts(newStage2.getOutputPort(), rightInputPort, 1);

			// make stages executable
			context.addThreadableStage(newStage1);
			context.addThreadableStage(newStage2);

		} catch (InstantiationException ie) {
			throw new RuntimeException(ie);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		}
	}

	/**
	 * Method to divide the given input.
	 *
	 * @param element
	 *            An element to be split and further processed
	 */
	protected abstract void divide(final I element);

	/**
	 * Method to join the given inputs together.
	 *
	 * @param eLeft
	 *            First half of the resulting element.
	 * @param eRight
	 *            Second half of the resulting element.
	 */
	protected abstract void conquer(final I eLeft, final I eRight);

	/**
	 * Determines whether or not to split the input problem by examining the given element
	 *
	 * @param element
	 *            The element whose properties determine the split condition
	 */
	protected abstract boolean splitCondition(final I element);
}
