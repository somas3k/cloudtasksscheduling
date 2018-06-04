package pl.edu.agh.io.cloudscheduling.entities;

import java.io.Serializable;

public abstract class CloudResult implements Serializable {
    private final long taskId;

    private final long vmId;

    public CloudResult(long taskId, long vmId) {
        this.taskId = taskId;
        this.vmId = vmId;
    }

    public long getTaskId() {
        return taskId;
    }

    public long getVmId() {
        return vmId;
    }
}
