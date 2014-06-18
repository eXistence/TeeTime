/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package teetime.examples.throughput.methodcall;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import teetime.examples.throughput.TimestampObject;
import teetime.examples.throughput.methodcall.stage.CollectorSink;
import teetime.examples.throughput.methodcall.stage.Distributor;
import teetime.examples.throughput.methodcall.stage.NoopFilter;
import teetime.examples.throughput.methodcall.stage.ObjectProducer;
import teetime.examples.throughput.methodcall.stage.Pipeline;
import teetime.examples.throughput.methodcall.stage.Relay;
import teetime.examples.throughput.methodcall.stage.StartTimestampFilter;
import teetime.examples.throughput.methodcall.stage.StopTimestampFilter;
import teetime.framework.core.Analysis;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class MethodCallThroughputAnalysis16 extends Analysis {

	private static final int NUM_WORKER_THREADS = Runtime.getRuntime().availableProcessors();

	private int numInputObjects;
	private Callable<TimestampObject> inputObjectCreator;
	private int numNoopFilters;

	private final List<List<TimestampObject>> timestampObjectsList = new LinkedList<List<TimestampObject>>();

	private Distributor<TimestampObject> distributor;
	private Thread producerThread;

	private Thread[] workerThreads;

	@Override
	public void init() {
		super.init();
		Runnable producerRunnable = this.buildProducerPipeline();
		this.producerThread = new Thread(producerRunnable);

		int numWorkerThreads = Math.min(NUM_WORKER_THREADS, 1); // only for testing purpose

		this.workerThreads = new Thread[numWorkerThreads];
		for (int i = 0; i < this.workerThreads.length; i++) {
			List<TimestampObject> resultList = new ArrayList<TimestampObject>(this.numInputObjects);
			this.timestampObjectsList.add(resultList);

			Runnable workerRunnable = this.buildPipeline(this.distributor, resultList);
			this.workerThreads[i] = new Thread(workerRunnable);
		}

		this.producerThread.start();

		try {
			this.producerThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private Runnable buildProducerPipeline() {
		final ObjectProducer<TimestampObject> objectProducer = new ObjectProducer<TimestampObject>(this.numInputObjects, this.inputObjectCreator);
		this.distributor = new Distributor<TimestampObject>();

		final Pipeline<Void, TimestampObject> pipeline = new Pipeline<Void, TimestampObject>();
		pipeline.setFirstStage(objectProducer);
		pipeline.setLastStage(this.distributor);

		UnorderedGrowablePipe.connect(objectProducer.getOutputPort(), this.distributor.getInputPort());

		return new RunnableStage(pipeline);
	}

	/**
	 * @param numNoopFilters
	 * @since 1.10
	 */
	private Runnable buildPipeline(final Distributor<TimestampObject> distributor, final List<TimestampObject> timestampObjects) {
		Relay<TimestampObject> relay = new Relay<TimestampObject>();
		@SuppressWarnings("unchecked")
		final NoopFilter<TimestampObject>[] noopFilters = new NoopFilter[this.numNoopFilters];
		// create stages
		final StartTimestampFilter startTimestampFilter = new StartTimestampFilter();
		for (int i = 0; i < noopFilters.length; i++) {
			noopFilters[i] = new NoopFilter<TimestampObject>();
		}
		final StopTimestampFilter stopTimestampFilter = new StopTimestampFilter();
		final CollectorSink<TimestampObject> collectorSink = new CollectorSink<TimestampObject>(timestampObjects);

		final Pipeline<TimestampObject, Object> pipeline = new Pipeline<TimestampObject, Object>();
		pipeline.setFirstStage(relay);
		pipeline.addIntermediateStage(startTimestampFilter);
		pipeline.addIntermediateStages(noopFilters);
		pipeline.addIntermediateStage(stopTimestampFilter);
		pipeline.setLastStage(collectorSink);

		OrderedGrowableArrayPipe.connect(distributor.getNewOutputPort(), relay.getInputPort());

		UnorderedGrowablePipe.connect(relay.getOutputPort(), startTimestampFilter.getInputPort());

		UnorderedGrowablePipe.connect(startTimestampFilter.getOutputPort(), noopFilters[0].getInputPort());
		for (int i = 0; i < noopFilters.length - 1; i++) {
			UnorderedGrowablePipe.connect(noopFilters[i].getOutputPort(), noopFilters[i + 1].getInputPort());
		}
		UnorderedGrowablePipe.connect(noopFilters[noopFilters.length - 1].getOutputPort(), stopTimestampFilter.getInputPort());
		UnorderedGrowablePipe.connect(stopTimestampFilter.getOutputPort(), collectorSink.getInputPort());

		return new RunnableStage(pipeline);
	}

	@Override
	public void start() {
		super.start();

		for (Thread workerThread : this.workerThreads) {
			workerThread.start();
		}

		try {
			for (Thread workerThread : this.workerThreads) {
				workerThread.join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setInput(final int numInputObjects, final Callable<TimestampObject> inputObjectCreator) {
		this.numInputObjects = numInputObjects;
		this.inputObjectCreator = inputObjectCreator;
	}

	public int getNumNoopFilters() {
		return this.numNoopFilters;
	}

	public void setNumNoopFilters(final int numNoopFilters) {
		this.numNoopFilters = numNoopFilters;
	}

	public List<List<TimestampObject>> getTimestampObjectsList() {
		return this.timestampObjectsList;
	}

}
