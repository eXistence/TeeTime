package teetime.framework.test;

import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import teetime.framework.OutputPort;
import teetime.stage.CollectorSink;

class Produces<T, O extends OutputPort<T>> extends BaseMatcher<O> {

	private final Matcher<Iterable<? extends T>> matcher;

	@SafeVarargs
	public Produces(final T... expectedElements) {
		this.matcher = Matchers.contains(expectedElements);
	}

	public Produces() {
		this.matcher = Matchers.emptyIterable();
	}

	@Override
	public boolean matches(final Object item) {
		if (!(item instanceof OutputPort)) {
			String message = String.format("%s", item);
			throw new IllegalArgumentException(message);
		}
		@SuppressWarnings("unchecked")
		OutputPort<T> outputPort = (OutputPort<T>) item;
		CollectorSink<T> collectorSink = StageFactory.getSinkFromOutputPort(outputPort);

		return matcher.matches(collectorSink.getElements());
		// the following invocation does not work with List<int[]>
		// return expectedElementsList.equals(collectorSink.getElements());
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("to produce ");
		matcher.describeTo(description);
	}

	@Override
	public void describeMismatch(final Object item, final Description description) {
		if (!(item instanceof OutputPort)) {
			String message = String.format("%s", item);
			throw new IllegalArgumentException(message);
		}
		@SuppressWarnings("unchecked")
		OutputPort<T> outputPort = (OutputPort<T>) item;
		CollectorSink<T> collectorSink = StageFactory.getSinkFromOutputPort(outputPort);

		List<T> actualElements = collectorSink.getElements();
		description.appendText("has produced ").appendValueList("[", ", ", "]", actualElements);
	}

}