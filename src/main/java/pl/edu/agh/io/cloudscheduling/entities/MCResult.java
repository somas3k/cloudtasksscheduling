package pl.edu.agh.io.cloudscheduling.entities;

public class MCResult extends CloudResult {
    private double result;

    public MCResult(long taskId, long vmId, double result) {
        super(taskId, vmId);
        this.result = result;
    }

    public double getResult() {
        return result;
    }

}
