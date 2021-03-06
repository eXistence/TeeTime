/**
 * Copyright © 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime-framework.github.io)
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
 */
package teetime.framework;

public enum StageState {

	/** First state of a stage */
	CREATED,
	/** Second state of a stage */
	INITIALIZED,
	/** Third state of a stage */
	VALIDATED,
	/** Fourth state of a stage */
	STARTED,
	/** Fifth state of a stage. Usually set in {@link InputPort#receive()} or by {@link AbstractStage#terminateStage()}. */
	TERMINATING,
	/** Sixth state of a stage. Usually set in {@link teetime.framework.AbstractStage#onTerminating()}. */
	TERMINATED;

	public boolean isBefore(final StageState stageState) {
		return this.compareTo(stageState) < 0;
	}

	public boolean isAfter(final StageState stageState) {
		return this.compareTo(stageState) > 0;
	}

}
