package com.airta.action.engine.model;

import com.airta.action.engine.entity.ResultType;

import java.io.IOException;
import java.util.List;

public interface Capturable {

    List<ResultType> requiredResults() throws IOException;

    ResultType capture() throws IOException;
}
