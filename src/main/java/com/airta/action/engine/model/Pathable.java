package com.airta.action.engine.model;

import com.airta.action.engine.entity.action.ActionPath;

import java.io.IOException;

public interface Pathable {

    void setPath(ActionPath actionPath) throws IOException;

    ActionPath getPath();
}
