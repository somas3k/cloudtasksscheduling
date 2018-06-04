package pl.edu.agh.io.cloudscheduling.entities;

public class JSResult extends CloudResult {
    private final int[][] tab;
    public JSResult(long taskId, long vmId, int[][] result) {
        super(taskId, vmId);
        tab = result;
    }

    public int[][] getTab() {
        return tab;
    }
}
