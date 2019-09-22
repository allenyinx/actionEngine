package com.airta.action.engine.parser.pattern;

import com.airta.action.engine.entity.ActionResult;
import com.airta.action.engine.model.Capturable;
import com.airta.action.engine.model.Datalize;
import com.airta.action.engine.model.Operatable;
import com.airta.action.engine.model.Pathable;

import java.io.IOException;

/**
 * @author allenyin
 */
public class FlowModel implements Operatable, Datalize, Pathable, Capturable {

    @Override
    public void read() {

    }

    @Override
    public void proceed() throws IOException {

    }

    @Override
    public ActionResult result() {
        return null;
    }
}
