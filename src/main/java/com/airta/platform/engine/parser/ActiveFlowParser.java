package com.airta.platform.engine.parser;

import com.airta.platform.engine.entity.Action;
import com.airta.platform.engine.entity.flow.FlowScript;
import com.airta.platform.engine.parser.pattern.IFlowParser;

import java.util.List;

/**
 * Generic flowScript parser.
 *
 * [xpath].type 123;
 * element.click 120;
 * time(a=12, b=13){
 *     element.click b;
 * }
 *
 * @author allenyin
 */
public class ActiveFlowParser implements IFlowParser {

    /**
     * @param flowPatternList
     */
    @Override
    public void setPattern(List<FlowPattern> flowPatternList) {

    }

    @Override
    public List<FlowPattern> getPattern() {
        return null;
    }

    @Override
    public boolean addPattern(FlowPattern flowPattern) {
        return false;
    }

    @Override
    public void setModel(List<FlowModel> flowModelList) {

    }

    @Override
    public List<FlowModel> getModel() {
        return null;
    }

    @Override
    public boolean addModel(FlowModel flowModel) {
        return false;
    }

    @Override
    public List<Action> parse(FlowScript flowScript) {
        return null;
    }

    @Override
    public List<Action> parse(List<FlowScript> flowScriptList) {
        return null;
    }

    /**
     * record the parse history.
     *
     * @param flowScript
     * @return add record result
     */
    @Override
    public boolean record(FlowScript flowScript) {
        return false;
    }
}
