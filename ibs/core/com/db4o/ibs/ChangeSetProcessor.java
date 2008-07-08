package com.db4o.ibs;

/**
 * A ChangeSetProcessor knows how to apply a {@link ChangeSet} produced by
 * its {@link ChangeSetBuilder} counterpart.
 * 
 * @see ChangeSetEngine
 */
public interface ChangeSetProcessor {

	void apply(ChangeSet changes);
}