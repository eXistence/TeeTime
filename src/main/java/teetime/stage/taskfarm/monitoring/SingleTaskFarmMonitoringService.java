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
package teetime.stage.taskfarm.monitoring;

import java.util.LinkedList;
import java.util.List;

import teetime.framework.pipe.IMonitorablePipe;
import teetime.stage.taskfarm.ITaskFarmDuplicable;
import teetime.stage.taskfarm.TaskFarmStage;
import teetime.stage.taskfarm.adaptation.history.TaskFarmHistoryService;
import teetime.stage.taskfarm.exception.TaskFarmInvalidPipeException;

/**
 * Represents a monitoring service for a task farm.
 *
 * @author Christian Claus Wiechmann
 */
public class SingleTaskFarmMonitoringService implements IMonitoringService<TaskFarmStage<?, ?, ?>, TaskFarmMonitoringData> {

	private static final long INIT = -1;

	/** time of monitoring start to calculate elapsed time **/
	private long startingTimestamp = INIT;

	/** monitored data **/
	private final List<TaskFarmMonitoringData> monitoredDatas = new LinkedList<TaskFarmMonitoringData>();
	/** monitored task farm **/
	private final TaskFarmStage<?, ?, ?> taskFarmStage;
	/** task farm history service to access the latest throughput measurement **/
	private final TaskFarmHistoryService<?, ?, ?> history;

	/** maximum number of worker stages used by the task farm over its whole execution **/
	private int maxNumberOfStages = 0;

	/**
	 * Constructor.
	 *
	 * @param taskFarmStage
	 *            task farm to be monitored
	 * @param history
	 *            task farm history service to access the latest throughput measurement
	 */
	public SingleTaskFarmMonitoringService(final TaskFarmStage<?, ?, ?> taskFarmStage, final TaskFarmHistoryService<?, ?, ?> history) {
		this.taskFarmStage = taskFarmStage;
		this.history = history;
	}

	@Override
	public List<TaskFarmMonitoringData> getData() {
		return this.monitoredDatas;
	}

	@Override
	public void addMonitoredItem(final TaskFarmStage<?, ?, ?> taskFarmStage) {
		throw new IllegalStateException("SingleTaskFarmMonitoringService can only monitor the one Task Farm given to the constructor.");
	}

	@Override
	public void doMeasurement() {
		long currentTimestamp = System.currentTimeMillis();
		if (this.startingTimestamp == INIT) {
			this.startingTimestamp = currentTimestamp;
		}

		TaskFarmMonitoringData monitoringData = new TaskFarmMonitoringData(currentTimestamp - this.startingTimestamp,
				this.taskFarmStage.getEnclosedStageInstances().size(),
				getMeanAndSumThroughput(this.taskFarmStage, MeanThroughputType.PULL, true),
				getMeanAndSumThroughput(this.taskFarmStage, MeanThroughputType.PUSH, true),
				getMeanAndSumThroughput(this.taskFarmStage, MeanThroughputType.PULL, false),
				getMeanAndSumThroughput(this.taskFarmStage, MeanThroughputType.PUSH, false),
				this.taskFarmStage.getConfiguration().getThroughputScoreBoundary());

		this.monitoredDatas.add(monitoringData);

		if (this.taskFarmStage.getEnclosedStageInstances().size() > this.maxNumberOfStages) {
			this.maxNumberOfStages = this.taskFarmStage.getEnclosedStageInstances().size();
		}
	}

	/**
	 * @return maximum number of worker stages used by the task farm over its whole execution
	 */
	public int getMaxNumberOfStages() {
		return this.maxNumberOfStages;
	}

	private enum MeanThroughputType {
		PUSH, PULL
	}

	private double getMeanAndSumThroughput(final TaskFarmStage<?, ?, ?> taskFarmStage, final MeanThroughputType type, final boolean mean) {
		double sum = 0;
		double count = 0;

		try {
			for (ITaskFarmDuplicable<?, ?> enclosedStage : taskFarmStage.getEnclosedStageInstances()) {
				IMonitorablePipe inputPipe = (IMonitorablePipe) enclosedStage.getInputPort().getPipe();
				if (inputPipe != null) {
					long pullThroughput = 0;
					long pushThroughput = 0;

					if (this.history == null) {
						pullThroughput = inputPipe.getPullThroughput();
						pushThroughput = inputPipe.getPushThroughput();
					} else {
						pullThroughput = this.history.getLastPullThroughputOfPipe(inputPipe);
						pushThroughput = this.history.getLastPushThroughputOfPipe(inputPipe);
					}

					switch (type) {
					case PULL:
						sum += pullThroughput;
						break;
					case PUSH:
						sum += pushThroughput;
						break;
					default:
						break;
					}

					count++;
				}
			}
		} catch (ClassCastException e) {
			throw new TaskFarmInvalidPipeException(
					"The input pipe of an enclosed stage instance inside a Task Farm"
							+ " does not implement IMonitorablePipe, which is required.");
		}

		// calculate the mean value if necessary
		if (mean) {
			if (count > 0) {
				sum /= count;
			}
		}

		return sum;
	}
}
