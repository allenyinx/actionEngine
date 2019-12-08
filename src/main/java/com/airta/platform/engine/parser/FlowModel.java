package com.airta.platform.engine.parser;

import com.airta.platform.engine.entity.action.ActionPath;
import com.airta.platform.engine.entity.action.ActionResult;
import com.airta.platform.engine.entity.ResultType;
import com.airta.platform.engine.entity.action.DataContext;
import com.airta.platform.engine.entity.action.PageContext;
import com.airta.platform.engine.model.*;
import com.airta.platform.engine.model.*;

import java.io.IOException;
import java.util.List;

/**
 * @author allenyin
 */
public class FlowModel implements Operatable, Datalizable, Pathable, Catchable, Recyclable {

    @Override
    public void read() {

    }

    @Override
    public String printData() {
        return null;
    }

    @Override
    public void proceed() throws IOException {

    }

    @Override
    public ActionResult result() {
        return null;
    }

    @Override
    public List<ResultType> requiredResults() throws IOException {
        return null;
    }

    @Override
    public ResultType capture() throws IOException {
        return null;
    }

    @Override
    public void setPath(ActionPath actionPath) throws IOException {

    }

    @Override
    public ActionPath getPath() {
        return null;
    }

    @Override
    public List<PageContext> getFurtherPageContexts() {
        return null;
    }

    @Override
    public List<DataContext> returnCurrentDataContext() {
        return null;
    }

    @Override
    public boolean recycle() {
        return false;
    }
}
