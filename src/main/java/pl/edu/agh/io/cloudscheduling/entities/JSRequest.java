package pl.edu.agh.io.cloudscheduling.entities;

public class JSRequest {
    private long numberOfIterations;
    private double zoom = 0;
    private int maxX = 800;
    private int maxY = 600;
    private double moveX = 0;
    private double moveY = 0;
    private double cX;
    private double cY;
    private double taskPriority;

    public double getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(double taskPriority) {
        this.taskPriority = taskPriority;
    }

    public JSRequest(long numberOfIterations, double cX, double cY) {
        this.numberOfIterations = numberOfIterations;
        this.cX = cX;
        this.cY = cY;
    }

    public JSRequest() {
    }

    public JSRequest(long numberOfIterations, double zoom, int maxX, int maxY, double moveX, double moveY, double cX, double cY) {
        this.numberOfIterations = numberOfIterations;
        this.zoom = zoom;
        this.maxX = maxX;
        this.maxY = maxY;
        this.moveX = moveX;
        this.moveY = moveY;
        this.cX = cX;
        this.cY = cY;
    }

    public long getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(long numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public double getMoveX() {
        return moveX;
    }

    public void setMoveX(double moveX) {
        this.moveX = moveX;
    }

    public double getMoveY() {
        return moveY;
    }

    public void setMoveY(double moveY) {
        this.moveY = moveY;
    }

    public double getcX() {
        return cX;
    }

    public void setcX(double cX) {
        this.cX = cX;
    }

    public double getcY() {
        return cY;
    }

    public void setcY(double cY) {
        this.cY = cY;
    }
}
