package pl.edu.agh.io.cloudscheduling.entities;



import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import pl.edu.agh.io.cloudscheduling.utils.TaskStatus;

import java.io.Serializable;

public abstract class CloudTask implements Comparable<CloudTask>, Serializable {
    private final long taskId;

    // virtual machine which will execute this task
    private transient VirtualMachine vm;

    // number of processor instructions needed to execute this task
    private transient long taskLength;

    private double startTime;

    private double finishTime;

    private transient double taskPriority;

    private TaskStatus status;

    private CloudResult result;

    private transient DeferredResult<ResponseEntity<CloudResult>> responseResult;

    public CloudResult getResult() {
        return result;
    }

    public void setResult(CloudResult result) {

        this.result = result;
    }

    public void setResponseResult(DeferredResult<ResponseEntity<CloudResult>> responseResult) {
        this.responseResult = responseResult;
    }

    public DeferredResult<ResponseEntity<CloudResult>> getResponseResult() {
        return responseResult;
    }

    public CloudTask(long taskId, long taskLength) {
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
        return Long.compare(taskId, cloudTask.taskId);
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

    public void setTaskPriority(double taskPriority) {
        this.taskPriority = taskPriority;
    }

    public CloudTask(long taskId) {
        this.taskId = taskId;
        this.status = TaskStatus.CREATED;
        this.vm = null;
    }

    public CloudTask(long taskId, double taskPriority) {
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

    public long getTaskId() {
        return taskId;

    }

    public double getTaskPriority() {
        return taskPriority;
    }

    public abstract void executeTask();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudTask cloudTask = (CloudTask) o;
        return taskId == cloudTask.taskId;
    }
}
