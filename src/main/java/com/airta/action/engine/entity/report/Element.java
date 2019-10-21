package com.airta.action.engine.entity.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Site map node element.
 * while contains depth, children, parent, status data.
 */
public class Element implements Serializable {

    private String elementId;
    private String pathPath;
    private int depth = 0;
    private ElementType type;
    private boolean actionable = true;
    private String parentId;
    private int childrenCount = 0;
    private List<Element> children = new ArrayList<>();
    private boolean isWorkingOn = false;
    private String url;

    public String toString() {

        return "\n[" + "\n" +
                "   elementId: " + getElementId() + "\n" +
                "   pathPath: " + getPathPath() + "\n" +
                "   type: " + getType() + "\n" +
                "   actionable: " + isActionable() + "\n" +
                "   parentId: " + getParentId() + "\n" +
                "   childrenCount: " + getChildrenCount() + "\n" +
                "   url: " + getUrl() + "\n" +
                "]\n";
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isActionable() {
        return actionable;
    }

    public void setActionable(boolean actionable) {
        this.actionable = actionable;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public void increaseChildrenCount() {
        this.childrenCount++;
    }

    public List<Element> getChildren() {
        return children;
    }

    public void setChildren(List<Element> children) {
        this.children = children;
    }

    public boolean isWorkingOn() {
        return isWorkingOn;
    }

    public void setWorkingOn(boolean workingOn) {
        isWorkingOn = workingOn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPathPath() {
        return pathPath;
    }

    public void setPathPath(String pathPath) {
        this.pathPath = pathPath;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }
}
