package com.airta.platform.engine.model;

import com.airta.platform.engine.entity.action.ActionPath;

import java.io.IOException;

public interface Pathable {

    void setPath(ActionPath actionPath) throws IOException;

    ActionPath getPath();
}
