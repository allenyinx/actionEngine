package com.airta.platform.engine.parser.pattern;

import com.airta.platform.engine.entity.Action;
import com.airta.platform.engine.entity.flow.FlowScript;
import com.airta.platform.engine.parser.FlowModel;
import com.airta.platform.engine.parser.FlowPattern;

import java.util.List;

/**
 * @author allenyin
 */
public interface IFlowParser {

    /**
     * @param flowPatternList
     */
    void setPattern(List<FlowPattern> flowPatternList);

    List<FlowPattern> getPattern();

    boolean addPattern(FlowPattern flowPattern);

    void setModel(List<FlowModel> flowModelList);

    List<FlowModel> getModel();

    boolean addModel(FlowModel flowModel);

    List<Action> parse(FlowScript flowScript);

    List<Action> parse(List<FlowScript> flowScriptList);

    /**
     * record the parse history.
     * @param flowScript
     * @return add record result
     */
    boolean record(FlowScript flowScript);

}
