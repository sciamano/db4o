/* Copyright (C) 2010  Versant Inc.   http://www.db4o.com */

package com.db4o.omplus.ui.dialog.login.presentation.test;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.junit.*;

import static org.easymock.EasyMock.*;

import com.db4o.omplus.ui.dialog.login.model.*;
import com.db4o.omplus.ui.dialog.login.presentation.*;

public class CustomConfigPaneTestCase {

	private Shell shell;
	private CustomConfigModel model;
	private CustomConfigPane configPane;
	
	@Before
	public void setUp() {
		shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		model = createMock(CustomConfigModel.class);
		configPane = new CustomConfigPane(shell, shell, model);
	}
	
	@After
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testAddRemove() {
		
	}
}