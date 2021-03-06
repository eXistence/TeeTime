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
package teetime.testutil;

import java.util.*;

/**
 *
 * @author Christian Wulf
 *
 */
public class ArrayCreator {

	private final Random random;

	public ArrayCreator(final long seed) {
		random = new Random(seed);
	}

	public int[] createFilledArray(final int numValues) {
		// ContiguousSet<Integer> inputNumbers = ContiguousSet.create(Range.closedOpen(0, 10), DiscreteDomain.integers());
		int[] randomValues = new int[numValues];

		for (int i = 0; i < randomValues.length; i++) {
			randomValues[i] = random.nextInt();
		}

		return randomValues;
	}

	public List<Integer> createFilledList(final int numValues) {
		// ContiguousSet<Integer> inputNumbers = ContiguousSet.create(Range.closedOpen(0, 10), DiscreteDomain.integers());
		List<Integer> randomValues = new ArrayList<Integer>(numValues);

		for (int i = 0; i < numValues; i++) {
			randomValues.add(random.nextInt());
		}

		return randomValues;
	}

}
