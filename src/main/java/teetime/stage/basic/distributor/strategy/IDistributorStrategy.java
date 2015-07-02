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
package teetime.stage.basic.distributor.strategy;

import teetime.framework.OutputPort;
import teetime.framework.OutputPortRemovedListener;

/**
 * @author Nils Christian Ehmke
 *
 * @since 1.0
 */
public interface IDistributorStrategy extends OutputPortRemovedListener {

	public <T> boolean distribute(final OutputPort<T>[] allOutputPorts, final T element);

}
