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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import teetime.stage.InitialElementProducer;
import teetime.stage.basic.Sink;
import teetime.testutil.AssertHelper;

/**
 * Tests whether
 * <ul>
 * <li>creating and connecting two stages within a composite stage works ({@link #ensureFlatteningDepth1AtRuntime()})
 * <li>flattening of a composite stage works ({@link #ensureFlatteningDepth1AtRuntime()})
 * <li>different levels of composite stages work ({@link #ensureFlatteningDepth2AtRuntime()})
 * <li>setting a stage active within a composite stage works ({@link #ensureWorkingCompositeStageWithInternalProducerAndActiveMerger()})
 * <li>creating a producer within a composite state works ({@link #ensureWorkingCompositeStageWithInternalProducerAndActiveMerger()})
 * </ul>
 *
 * @author Christian Wulf
 *
 */
public class AbstractCompositeStageTest {

	@Before
	public void before() {
		AbstractStage.clearInstanceCounters(); // resets the id to zero
	}

	@Test
	public void ensureFlatteningDepth1AtRuntime() {
		CounterContainer element = new CounterContainer();
		Execution<CompositeCounterPipelineConfig> execution = new Execution<CompositeCounterPipelineConfig>(new CompositeCounterPipelineConfig(1, element));

		InitialElementProducer<CounterContainer> producer;
		CounterIncrementer stage;

		producer = assertFirstStage(execution);
		stage = assertSecondStage(producer);
		assertLastStage(stage);
	}

	@Test
	public void ensureFlatteningDepth2AtRuntime() {
		CounterContainer element = new CounterContainer();
		Execution<CompositeCounterPipelineConfig> execution = new Execution<CompositeCounterPipelineConfig>(new CompositeCounterPipelineConfig(2, element));

		InitialElementProducer<CounterContainer> producer;
		CounterIncrementer stage;

		producer = assertFirstStage(execution);
		stage = assertSecondStage(producer);
		stage = assertThirdStage(stage);
		assertLastStage(stage);
	}

	@Test
	public void ensureWorkingCompositeStageWithInternalProducerAndActiveMerger() {
		Execution<CompositeProducerConfig> execution = new Execution<CompositeProducerConfig>(new CompositeProducerConfig());
		execution.executeBlocking();

		assertThat(execution.getConfiguration().getResultElements(), is(Arrays.asList(5, 0, 6, 1, 7, 2, 8, 3, 9, 4)));
	}

	private InitialElementProducer<CounterContainer> assertFirstStage(final Execution<CompositeCounterPipelineConfig> execution) {
		InitialElementProducer<CounterContainer> producer = execution.getConfiguration().getProducer();
		assertThat(producer.getId(), is(equalTo("InitialElementProducer-0")));
		return producer;
	}

	private CounterIncrementer assertSecondStage(final InitialElementProducer<CounterContainer> producer) {
		AbstractStage nextStage = producer.getOutputPort().getPipe().getTargetPort().getOwningStage();
		CounterIncrementer stage = AssertHelper.assertInstanceOf(CounterIncrementer.class, nextStage);
		assertThat(stage.getId(), is(equalTo("CounterIncrementer-0")));
		return stage;
	}

	private CounterIncrementer assertThirdStage(CounterIncrementer stage) {
		AbstractStage nextStage = stage.getOutputPort().getPipe().getTargetPort().getOwningStage();
		stage = AssertHelper.assertInstanceOf(CounterIncrementer.class, nextStage);
		assertThat(stage.getId(), is(equalTo("CounterIncrementer-1")));
		return stage;
	}

	private void assertLastStage(final CounterIncrementer stage) {
		AbstractStage nextStage;
		nextStage = stage.getOutputPort().getPipe().getTargetPort().getOwningStage();
		Sink<?> sink = AssertHelper.assertInstanceOf(Sink.class, nextStage);
		assertThat(sink.getId(), is(equalTo("Sink-0")));
	}
}
