package com.airta.action.engine.parser;

import com.airta.action.engine.entity.Action;
import com.airta.action.engine.entity.flow.FlowScript;
import com.airta.action.engine.parser.pattern.IFlowParser;

import java.util.List;

/**
 * exploit ANTLR for lexer and parser.
 *
 * @see 'https://www.antlr.org/'
 *
 * ANTLR (ANother Tool for Language Recognition) is a powerful parser generator
 * for reading, processing, executing, or translating structured text or binary files.
 * It's widely used to build languages, tools, and frameworks.
 * From a grammar, ANTLR generates a parser that can build and walk parse trees.
 *
 * @author allenyin
 */
public class AntlrFlowParser implements IFlowParser {

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
