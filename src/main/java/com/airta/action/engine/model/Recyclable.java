package com.airta.action.engine.model;

import com.airta.action.engine.entity.action.DataContext;
import com.airta.action.engine.entity.action.PageContext;

import java.util.List;

/**
 * The action needs to be recycled, means there should be further contexts in the current page.
 * and need to respond to task manager for more schedule.
 * @author allenyin
 */
public interface Recyclable {

    List<PageContext> getFurtherPageContexts();

    List<DataContext> returnCurrentDataContext();
}
