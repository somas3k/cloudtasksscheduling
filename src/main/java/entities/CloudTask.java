package entities;

import java.util.concurrent.Callable;

public abstract class CloudTask {
    private final int taskId;

    // id of virtual machine which will execute this task
    private int vmId;

    // number of processor instructions needed to execute this task
    private long taskLength;

    private double startTime;

    private double finishTime;

    private int taskPriority;

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public CloudTask(int taskId, int vmId, long taskLength) {
        this.taskId = taskId;
        this.vmId = vmId;
        this.taskLength = taskLength;
    }

    public void setVmId(int vmId) {
        this.vmId = vmId;
    }

    public long getTaskLength() {
        return taskLength;
    }

    public void setTaskLength(long taskLength) {
        this.taskLength = taskLength;
    }

    public int getTaskId() {
        return taskId;

    }

    public abstract void executeTask();


}
