/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 */
package com.db4o.browser.gui.controllers.query;

import org.eclipse.swt.custom.CTabFolder;

import com.db4o.browser.gui.controllers.BrowserController;
import com.db4o.browser.gui.controllers.QueryController;
import com.db4o.browser.gui.views.QueryBrowserPane;
import com.db4o.browser.model.BrowserCore;
import com.db4o.reflect.ReflectClass;

public class QueryTabController extends BrowserController {
    
    private BrowserController browserController;

    public QueryTabController(QueryController queryController, CTabFolder folder, QueryBrowserPane ui, ReflectClass clazz) {
        super(ui, queryController);
        this.browserController = queryController.getBrowserController();
    }
    
    protected void addQueryButtonHandler() {
    }

    public void setInput(ReflectClass clazz) {
        setInput(BrowserCore.getDefault().iterator(browserController.getCurrentFile(), clazz.getName()), null);
    }

}
