package decaf.tests.functional.ant;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.tools.ant.*;

public class ApiDiffTask extends Task {
	
	private File _from;
	private File _to;

	public void setFrom(File from) {
		_from = from;
	}
	
	public void setTo(File to) {
		_to = to;
	}

	@Override
	public void execute() throws BuildException {
		if (null == _from) throw new IllegalStateException("Missing 'from'.");
		if (null == _to) throw new IllegalStateException("Missing 'to'.");
		
		try {
			final Set<String> fromEntries = classEntries(_from);
			final Set<String> toEntries = classEntries(_to);
			
			final Set<String> unexpected = allBut(toEntries, fromEntries);
			logAll("Unexpected class: ", unexpected);
			
			final Set<String> missing = allBut(fromEntries, toEntries);
			logAll("Missing class: ", missing);
			
			if (!unexpected.isEmpty() || !missing.isEmpty()) {
				throw new BuildException("API surfaces do not match. Diferences have been reported.", getLocation());
			}
		} catch (IOException e) {
			throw new BuildException(e, getLocation());
		}	
	}

	private void logAll(String prefix, Set<String> set) {
		if (set.size() > 0) {
			for (String klass : set) {
				log(prefix + klass);
			}
		}
	}

	private Set<String> allBut(final Set<String> source,
			final Set<String> exclude) {
		HashSet<String> clone = new HashSet<String>(source);
		clone.removeAll(exclude);
		return clone;
	}

	private Set<String> classEntries(File file) throws IOException {
		final Set<String> found = new HashSet<String>();
		final ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
		try {
			ZipEntry entry = null;
			while (null != (entry = zip.getNextEntry())) {
				if (entry.getName().endsWith(".class")
					&& entry.getName().indexOf('$') == -1) {
					found.add(entry.getName());
				}
			}
		} finally {
			zip.close();
		}
		return found;
	}
}