package entities;

import utils.TaskStatus;

import java.io.Serializable;

public abstract class CloudTask implements Comparable<CloudTask>, Serializable {
    private final int taskId;

    // virtual machine which will execute this task
    private transient VirtualMachine vm;

    // number of processor instructions needed to execute this task
    private long taskLength;

    private double startTime;

    private double finishTime;

    private int taskPriority;

    private TaskStatus status;

    public CloudTask(int taskId, long taskLength) {
        this.taskId = taskId;
        this.taskLength = taskLength;
        this.status = TaskStatus.CREATED;
        this.vm = null;
    }

    public synchronized TaskStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public int compareTo(CloudTask cloudTask) {
        return Integer.compare(taskId, cloudTask.taskId);
    }

    @Override
    public String toString() {
        if(vm!= null) return "CloudTask{" +
                "taskId=" + taskId +
                ", vm=" + vm +
                ", taskLength=" + taskLength +
                ", taskPriority=" + taskPriority +
                ", status=" + status +
                '}';
        else return "CloudTask{" +
                "taskId=" + taskId +
                ", vm=" + "NOT_ASSIGNED" +
                ", taskLength=" + taskLength +
                ", taskPriority=" + taskPriority +
                ", status=" + status +
                '}';
    }

    public CloudTask(int taskId, int taskPriority) {
        this.taskId = taskId;
        this.taskPriority = taskPriority;
        this.status = TaskStatus.CREATED;
        this.vm = null;
    }

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

    public VirtualMachine getVm() {
        return vm;
    }

    public void setVm(VirtualMachine vm) {
        this.vm = vm;
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

    public int getTaskPriority() {
        return taskPriority;
    }

    public abstract void executeTask();


}
