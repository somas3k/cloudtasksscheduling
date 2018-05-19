package tasks;

import entities.CloudTask;
import utils.SerializableFunction;

import java.io.Serializable;
import java.util.Random;
import java.util.function.Function;

public class MonteCarloTask extends CloudTask {

    private SerializableFunction<Double, Double> function;
    private long numberOfIterations;

    private double result;
    private BoundUtil bound;


    public MonteCarloTask(int taskId, long numberOfIterations, int priority, SerializableFunction<Double, Double> function, double minX, double maxX) {
        super(taskId, priority);
        this.bound = new BoundUtil(minX, maxX);
        this.numberOfIterations = numberOfIterations;
        this.function = function;
        this.setTaskLength(calculateTaskLength(numberOfIterations, function, bound));
    }

    private static long calculateTaskLength(long numberOfIterations, Function<Double, Double> function, BoundUtil bound){

        long start = System.nanoTime();
        double result = 0;
        for(int i = 0; i < 100; ++i){
            double x = bound.getRandomX();
            result += function.apply(x);
        }
        result *= bound.getDx() / numberOfIterations;
        long stop = System.nanoTime();
        long length = stop - start;
        if(result!=0) {
            length /= 100;
            length *= numberOfIterations;
        }
        return length;
    }

    @Override
    public void executeTask() {
        result = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            double x = bound.getRandomX();
            result += function.apply(x);
        }
        result *= bound.getDx() / numberOfIterations;

        System.out.println(result);


    }

    private static class BoundUtil implements Serializable {
        final double minX;
        final double maxX;

        BoundUtil(double minX, double maxX) {
            this.minX = minX;
            this.maxX = maxX;
            random = new Random();
        }

        private Random random;

        double getRandomX(){
            return random.nextDouble() * (maxX-minX) + minX;
        }

        double getDx(){
            return maxX - minX;
        }


    }
}
