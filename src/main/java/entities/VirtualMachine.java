package entities;

public class VirtualMachine {
    private final int vmId;

    private int numberOfAssignedTasks;

    // number of millions instructions per second
    private double mipsValue;

    public VirtualMachine(int vmId, double mipsValue) {
        this.vmId = vmId;
        this.mipsValue = mipsValue;
        this.numberOfAssignedTasks = 0;
    }

    public int getVmId() {
        return vmId;
    }

    public double getMipsValue() {
        return mipsValue;
    }

    public double getValueToCalculatePriority() {
        return mipsValue / (double)(numberOfAssignedTasks+1);
    }
}
