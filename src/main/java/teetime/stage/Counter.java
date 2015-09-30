/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime-framework.github.io)
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
package teetime.stage;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

public final class Counter<T> extends AbstractConsumerStage<T> {

	private final OutputPort<T> outputPort = this.createOutputPort();

	private int numElementsPassed;

	@Override
	protected void execute(final T element) {
		this.numElementsPassed++;

		outputPort.send(element);
	}

	/**
	 * <i>Hint:</i> This method may not be invoked by another thread since it is not thread-safe.
	 *
	 * @return the number of passed elements
	 */
	public int getNumElementsPassed() {
		return this.numElementsPassed;
	}

	public OutputPort<T> getOutputPort() {
		return this.outputPort;
	}
}
