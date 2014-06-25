package teetime.variant.methodcallWithPorts.framework.core.pipe;

import teetime.util.list.CommittableResizableArrayQueue;
import teetime.variant.methodcallWithPorts.framework.core.InputPort;
import teetime.variant.methodcallWithPorts.framework.core.OutputPort;

public class Pipe<T> extends AbstractPipe<T> {

	private final CommittableResizableArrayQueue<T> elements = new CommittableResizableArrayQueue<T>(null, 4);

	public static <T> void connect(final OutputPort<T> sourcePort, final InputPort<T> targetPort) {
		IPipe<T> pipe = new Pipe<T>();
		sourcePort.setPipe(pipe);
		targetPort.setPipe(pipe);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see teetime.examples.throughput.methodcall.IPipe#add(T)
	 */
	@Override
	public void add(final T element) {
		this.elements.addToTailUncommitted(element);
		this.elements.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see teetime.examples.throughput.methodcall.IPipe#removeLast()
	 */
	@Override
	public T removeLast() {
		T element = this.elements.removeFromHeadUncommitted();
		this.elements.commit();
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see teetime.examples.throughput.methodcall.IPipe#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see teetime.examples.throughput.methodcall.IPipe#readLast()
	 */
	@Override
	public T readLast() {
		return this.elements.getTail();
	}

	public CommittableResizableArrayQueue<T> getElements() {
		return this.elements;
	}

	@Override
	public int size() {
		return this.elements.size();
	}

}