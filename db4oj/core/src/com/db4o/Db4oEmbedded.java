package com.db4o;

import com.db4o.config.*;
import com.db4o.ext.*;
import com.db4o.foundation.*;
import com.db4o.internal.*;

public class Db4oEmbedded {

	/**
	 * Creates a fresh {@link Configuration Configuration} instance.
	 * 
	 * @return a fresh, independent configuration with all options set to their default values
	 */
	public static Configuration newConfiguration() {
		Config4Impl config = new Config4Impl();
		Platform4.getDefaultConfiguration(config);
		return config;
	}

	/**
	 * opens an {@link ObjectContainer ObjectContainer}
	 * on the specified database file for local use.
	 * <br><br>A database file can only be opened once, subsequent attempts to open
	 * another {@link ObjectContainer ObjectContainer} against the same file will result in
	 * a {@link DatabaseFileLockedException DatabaseFileLockedException}.<br><br>
	 * Database files can only be accessed for readwrite access from one process 
	 * (one Java VM) at one time. All versions except for db4o mobile edition use an
	 * internal mechanism to lock the database file for other processes. 
	 * <br><br>
	 * @param config a custom {@link Configuration Configuration} instance to be obtained via {@link newConfiguration}
	 * @param databaseFileName an absolute or relative path to the database file
	 * @return an open {@link ObjectContainer ObjectContainer}
	 * @see Configuration#readOnly
	 * @see Configuration#encrypt
	 * @see Configuration#password
	 * @throws Db4oIOException I/O operation failed or was unexpectedly interrupted.
	 * @throws DatabaseFileLockedException the required database file is locked by 
	 * another process.
	 * @throws IncompatibleFileFormatException runtime 
	 * {@link com.db4o.config.Configuration configuration} is not compatible
	 * with the configuration of the database file. 
	 * @throws OldFormatException open operation failed because the database file
	 * is in old format and {@link com.db4o.config.Configuration#allowVersionUpdates(boolean)} 
	 * is set to false.
	 * @throws DatabaseReadOnlyException database was configured as read-only.
	 */
	public static final ObjectContainer openFile(Configuration config,
			String databaseFileName) throws Db4oIOException,
			DatabaseFileLockedException, IncompatibleFileFormatException,
			OldFormatException, DatabaseReadOnlyException {
		if (null == config) {
			throw new ArgumentNullException();
		}
		return ObjectContainerFactory.openObjectContainer(config,
				databaseFileName);
	}

}