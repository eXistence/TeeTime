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
package teetime.stage.taskfarm.adaptation.analysis;

import teetime.stage.taskfarm.ITaskFarmDuplicable;
import teetime.stage.taskfarm.TaskFarmConfiguration;
import teetime.stage.taskfarm.adaptation.history.ThroughputHistory;

public class TaskFarmAnalyzer<I, O, T extends ITaskFarmDuplicable<I, O>> {

	private final TaskFarmConfiguration<I, O, T> configuration;
	private double throughputScore;

	public TaskFarmAnalyzer(final TaskFarmConfiguration<I, O, T> configuration) {
		this.configuration = configuration;
	}

	public void analyze(final ThroughputHistory history) {
		AbstractThroughputAnalysisAlgorithm algorithm = null;

		// FIXME
		// ThroughputAlgorithm throughputAlgorithm = configuration.getThroughputAlgorithm();
		// algorithm = throughputAlgorithm.create(configuration);

		switch (configuration.getThroughputAlgorithm()) {
		case MEAN:
			algorithm = new MeanAlgorithm(configuration);
			break;
		case WEIGHTED:
			algorithm = new WeightedAlgorithm(configuration);
			break;
		case REGRESSION:
			algorithm = new RegressionAlgorithm(configuration);
			break;
		default:
			algorithm = new RegressionAlgorithm(configuration);
			break;
		}

		throughputScore = algorithm.doAnalysis(history);
	}

	public double getThroughputScore() {
		return throughputScore;
	}

}
