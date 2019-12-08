package com.airta.platform.engine.runtime;

public class TaskResult {

  private TaskStatus status = TaskStatus.INITED;
  private String taskId = null;
  private long startTime = 0;
  private long endTime = 0;
  private long lastUpdate = 0;
  private int startNode = 0;
  public TaskResult(TaskStatus status, String taskId, long lastUpdate, int startNode){
    this.status = status;
    this.taskId = taskId;
    this.lastUpdate = lastUpdate;
    this.startNode = startNode;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }



  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }


  public int getStartNode() {
    return startNode;
  }

  public void setStartNode(int startNode) {
    this.startNode = startNode;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }
}
