package com.airta.action.engine.model;

import com.airta.action.engine.entity.ResultType;

import java.io.IOException;
import java.util.List;

/**
 * @author allenyin
 */
public interface Catchable {

    /**
     * get the required capture type.
     * i.e. screenshot, jslog.
     * @return List
     * @throws IOException
     */
    List<ResultType> requiredResults() throws IOException;

    /**
     * do the actual capture operation.
     * @return ResultType
     * @throws IOException
     */
    ResultType capture() throws IOException;
}
