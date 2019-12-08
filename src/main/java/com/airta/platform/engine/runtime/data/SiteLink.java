package com.airta.platform.engine.runtime.data;

public class SiteLink {
  private int id = 0;
  private int startNode = 0;
  private int endNode = 0;
  private String stepIn = null; // the stepIn is the script logic that navigat from start to the end Node
  private int stepTime = 0; // used for evaluation of cost

  public SiteLink(int startNode, int endNode, String stepIn, int stepTime){
    this.stepIn = stepIn;
    this.startNode = startNode;
    this.endNode = endNode;
    this.stepTime = stepTime;
  }
  public int getStepTime() {
    return stepTime;
  }

  public void setStepTime(int stepTime) {
    this.stepTime = stepTime;
  }

  public int getStartNode() {
    return startNode;
  }

  public void setStartNode(int startNode) {
    this.startNode = startNode;
  }

  public int getEndNode() {
    return endNode;
  }

  public void setEndNode(int endNode) {
    this.endNode = endNode;
  }

  public String getStepIn() {
    return stepIn;
  }

  public void setStepIn(String stepIn) {
    this.stepIn = stepIn;
  }

  public SiteLink copyOf(){
    SiteLink ret = new SiteLink(startNode,endNode,stepIn,stepTime);
    ret.id = id;
    return ret;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
