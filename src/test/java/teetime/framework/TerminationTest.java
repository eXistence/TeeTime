/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://christianwulf.github.io/teetime)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teetime.framework;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import teetime.stage.InitialElementProducer;
import teetime.stage.basic.Sink;

public class TerminationTest {

	@Test(timeout = 1000)
	public void correctAbort() throws InterruptedException {
		TerminationConfig configuration = new TerminationConfig(10);
		Execution<TerminationConfig> execution = new Execution<TerminationConfig>(configuration);
		execution.executeNonBlocking();
		execution.abortEventually();
	}

	@Test(timeout = 3000)
	public void doesNotGetStuckInAdd() throws InterruptedException {
		TerminationConfig configuration = new TerminationConfig(1);
		Execution<TerminationConfig> execution = new Execution<TerminationConfig>(configuration);
		execution.executeNonBlocking();
		Thread.sleep(100);
		execution.abortEventually();
		assertThat(configuration.finalProp.time - 450, is(greaterThan(configuration.firstProp.time)));
	}

	private class TerminationConfig extends Configuration {
		InitialElementProducer<Integer> init = new InitialElementProducer<Integer>(1, 2, 3, 4, 5, 6);
		Propagator firstProp = new Propagator();
		DoesNotRetrieveElements sinkStage = new DoesNotRetrieveElements();
		Propagator finalProp = new Propagator();

		public TerminationConfig(final int capacity) {
			if (capacity == 1) {
				connectPorts(init.getOutputPort(), firstProp.getInputPort());
				connectPorts(firstProp.getOutputPort(), sinkStage.getInputPort(), capacity);
				connectPorts(sinkStage.getOutputPort(), finalProp.getInputPort());
				sinkStage.declareActive();
			} else {
				Sink<Integer> sink = new Sink<Integer>();
				connectPorts(init.getOutputPort(), sink.getInputPort(), capacity);
				sink.declareActive();
			}
		}

	}

	private class DoesNotRetrieveElements extends AbstractConsumerStage<Integer> {

		private final OutputPort<Integer> output = createOutputPort();

		@Override
		protected void execute(final Integer element) {
			int i = 0;
			while (true) {
				i++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// First sleep will throw this
				}
				if (i > 1) {
					super.terminate();
					break;
				}
			}

		}

		public OutputPort<? extends Integer> getOutputPort() {
			return output;
		}

		@Override
		protected void terminate() {}

		@Override
		protected void abort() {}

	}

	private class Propagator extends AbstractConsumerStage<Integer> {

		public long time;
		private final OutputPort<Integer> output = createOutputPort();

		@Override
		protected void execute(final Integer element) {
			output.send(element);
		}

		public OutputPort<? extends Integer> getOutputPort() {
			return output;
		}

		@Override
		public void onTerminating() throws Exception {
			time = System.currentTimeMillis();
			super.onTerminating();
		}
	}

}