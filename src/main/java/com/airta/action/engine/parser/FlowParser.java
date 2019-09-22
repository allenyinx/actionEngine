package com.airta.action.engine.parser;

import com.airta.action.engine.entity.Action;
import com.airta.action.engine.entity.flow.FlowScript;
import com.airta.action.engine.parser.pattern.FlowModel;
import com.airta.action.engine.parser.pattern.FlowPattern;

import java.util.List;

/**
 * @author allenyin
 */
public interface FlowParser {

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

}
