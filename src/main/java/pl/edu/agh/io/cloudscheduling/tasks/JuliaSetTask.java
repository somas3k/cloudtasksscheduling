package pl.edu.agh.io.cloudscheduling.tasks;

import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.JSRequest;
import pl.edu.agh.io.cloudscheduling.entities.JSResult;
import pl.edu.agh.io.cloudscheduling.VMResource;

import java.awt.*;

public class JuliaSetTask extends CloudTask {

    private final long numberOfIterations;
    private double zoom;
    private int maxX;
    private int maxY;
    private double moveX;
    private double moveY;
    private final double cX;
    private final double cY;

    public JuliaSetTask(long taskId, JSRequest request) {
        super(taskId, request.getTaskPriority());
        numberOfIterations = request.getNumberOfIterations();
        cX = request.getcX();
        cY = request.getcY();
        zoom = request.getZoom();
        maxX = request.getMaxX();
        maxY = request.getMaxY();
        moveX = request.getMoveX();
        moveY = request.getMoveY();
    }

    @Override
    public void executeTask() {
        int[][] table = new int[maxX][maxY];
        double zx, zy;
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                zx = 1.5 * (x - maxX / 2) / (0.5 * zoom * maxX) + moveX;
                zy = (y - maxY / 2) / (0.5 * zoom * maxY) + moveY;
                float i = numberOfIterations;
                while (zx * zx + zy * zy < 4 && i > 0) {
                    double tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    i--;
                }
                int c = Color.HSBtoRGB((numberOfIterations / i) % 1, 1, i > 0 ? 1 : 0);
                table[x][y] = c;
            }
        }
        setResult(new JSResult(getTaskId(), VMResource.vmId.get(), table));
    }
}
