package com.airta.action.engine.model;

import com.airta.action.engine.entity.action.ActionResult;

import java.io.IOException;

public interface Operatable {

    void proceed() throws IOException;

    ActionResult result();
}
