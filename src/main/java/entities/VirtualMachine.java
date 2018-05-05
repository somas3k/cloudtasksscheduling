package entities;

public class VirtualMachine {
    private final int vmId;

    // number of millions instructions per second
    private double mipsValue;

    public VirtualMachine(int vmId, double mipsValue) {
        this.vmId = vmId;
        this.mipsValue = mipsValue;
    }

    public int getVmId() {
        return vmId;
    }

    public double getMipsValue() {
        return mipsValue;
    }
}
