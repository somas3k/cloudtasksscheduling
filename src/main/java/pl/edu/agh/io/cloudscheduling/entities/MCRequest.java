package pl.edu.agh.io.cloudscheduling.entities;

public class MCRequest {
    private String function;
    private int numberOfIterations;
    private double minX;
    private double maxX;
    private double priority;

    public MCRequest(String function, int numberOfIterations, double minX, double maxX, double priority) {
        this.function = function;
        this.numberOfIterations = numberOfIterations;
        this.minX = minX;
        this.maxX = maxX;
        this.priority = priority;
    }

    public MCRequest() {
    }

    public String getFunction() {

        return function;
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getPriority() {
        return priority;
    }
}
