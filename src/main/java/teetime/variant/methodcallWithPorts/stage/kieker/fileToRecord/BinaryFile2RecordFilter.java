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
package teetime.variant.methodcallWithPorts.stage.kieker.fileToRecord;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import teetime.variant.methodcallWithPorts.framework.core.ConsumerStage;
import teetime.variant.methodcallWithPorts.stage.kieker.className.ClassNameRegistryRepository;

import kieker.common.exception.MonitoringRecordException;
import kieker.common.record.IMonitoringRecord;
import kieker.common.util.filesystem.BinaryCompressionMethod;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class BinaryFile2RecordFilter extends ConsumerStage<File, IMonitoringRecord> {

	private static final int MB = 1024 * 1024;

	private RecordFromBinaryFileCreator recordFromBinaryFileCreator;

	private ClassNameRegistryRepository classNameRegistryRepository;

	/**
	 * @since 1.10
	 */
	public BinaryFile2RecordFilter(final ClassNameRegistryRepository classNameRegistryRepository) {
		this();
		this.classNameRegistryRepository = classNameRegistryRepository;
	}

	/**
	 * @since 1.10
	 */
	public BinaryFile2RecordFilter() {
		super();
	}

	@Override
	public void onStart() {
		this.recordFromBinaryFileCreator = new RecordFromBinaryFileCreator(this.logger, this.classNameRegistryRepository);
		super.onStart();
	}

	public ClassNameRegistryRepository getClassNameRegistryRepository() {
		return this.classNameRegistryRepository;
	}

	public void setClassNameRegistryRepository(final ClassNameRegistryRepository classNameRegistryRepository) {
		this.classNameRegistryRepository = classNameRegistryRepository;
	}

	@Override
	protected void execute5(final File binaryFile) {
		try {
			final BinaryCompressionMethod method = BinaryCompressionMethod.getByFileExtension(binaryFile.getName());
			final DataInputStream in = method.getDataInputStream(binaryFile, 1 * MB);
			try {
				IMonitoringRecord record = this.recordFromBinaryFileCreator.createRecordFromBinaryFile(binaryFile, in);
				while (record != null) {
					this.send(record);
					record = this.recordFromBinaryFileCreator.createRecordFromBinaryFile(binaryFile, in);
				}
			} catch (final MonitoringRecordException e) {
				this.logger.error("Error reading file: " + binaryFile, e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (final IOException ex) {
						this.logger.error("Exception while closing input stream for processing input file", ex);
					}
				}
			}
		} catch (final IOException e) {
			this.logger.error("Error reading file: " + binaryFile, e);
		} catch (final IllegalArgumentException e) {
			this.logger.warn("Unknown file extension for file: " + binaryFile);
		}
	}

}