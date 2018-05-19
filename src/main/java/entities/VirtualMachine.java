package entities;

public class VirtualMachine implements Comparable<VirtualMachine>{
    private final int vmId;

    private int numberOfAssignedTasks;

    // number of millions instructions per second
    private double mipsValue;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public VirtualMachine(int vmId, double mipsValue, String key) {
        this.vmId = vmId;
        this.mipsValue = mipsValue;
        this.key = key;
        this.numberOfAssignedTasks = 0;
    }

    public void incNumberOfAssignedTasks(){
        numberOfAssignedTasks++;
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

    @Override
    public String toString() {
        return "VirtualMachine{" +
                "vmId=" + vmId +
                ", numberOfAssignedTasks=" + numberOfAssignedTasks +
                ", mipsValue=" + mipsValue +
                '}';
    }

    @Override
    public int compareTo(VirtualMachine o) {
        return Integer.compare(vmId, o.vmId);
    }
}
