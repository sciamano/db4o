package db4ounit.db4o;

import com.db4o.ext.ExtObjectContainer;

import db4ounit.TestCase;
import db4ounit.TestLifeCycle;

public class Db4oTestCase implements TestCase, TestLifeCycle {
    
	private Db4oFixture _fixture;
	
	public void fixture(Db4oFixture fixture) {
		_fixture = fixture;
	}

	public Db4oFixture fixture() {
		return _fixture;
	}
	
	public void setUp() throws Exception {
        _fixture.clean();
		configure();
		_fixture.open();
		store();
        _fixture.close();
        _fixture.open();
	}
	
	public void tearDown() throws Exception {
		_fixture.close();
        _fixture.clean();
	}

	protected void configure() {}
	
	public void store() {}

	protected ExtObjectContainer db() {
		return fixture().db();
	}
}
