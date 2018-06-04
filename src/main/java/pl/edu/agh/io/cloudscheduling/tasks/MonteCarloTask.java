package pl.edu.agh.io.cloudscheduling.tasks;

import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.MCRequest;
import pl.edu.agh.io.cloudscheduling.entities.MCResult;
import pl.edu.agh.io.cloudscheduling.utils.SerializableFunction;
import pl.edu.agh.io.cloudscheduling.virtual_machine.VMResource;
import pl.joegreen.lambdaFromString.LambdaCreationException;
import pl.joegreen.lambdaFromString.LambdaFactory;
import pl.joegreen.lambdaFromString.LambdaFactoryConfiguration;
import pl.joegreen.lambdaFromString.TypeReference;

import java.io.Serializable;
import java.util.Random;
import java.util.function.Function;

public class MonteCarloTask extends CloudTask {

    private String functionString;
    private long numberOfIterations;

    private BoundUtil bound;

    public MonteCarloTask(long taskId, MCRequest request) {
        super(taskId);
        this.numberOfIterations = request.getNumberOfIterations();
        this.bound = new BoundUtil(request.getMinX(), request.getMaxX());
        super.setTaskPriority(request.getPriority());
            this.functionString = request.getFunction();

        this.setTaskLength(calculateTaskLength(numberOfIterations, functionString, bound));
    }

    public MonteCarloTask(int taskId, long numberOfIterations, int priority, SerializableFunction<Double, Double> function, double minX, double maxX) {
        super(taskId, priority);
        this.bound = new BoundUtil(minX, maxX);
        this.numberOfIterations = numberOfIterations;
        //this.function = function;
        //this.setTaskLength(calculateTaskLength(numberOfIterations, function, bound));
    }

    private static long calculateTaskLength(long numberOfIterations, String functionString, BoundUtil bound){

        Function<Double, Double> function;
        try {
            function= LambdaFactory.get(LambdaFactoryConfiguration.get().withImports(Math.class)).createLambda(functionString,new TypeReference<Function<Double, Double>>(){});
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
        } catch (LambdaCreationException e) {
            e.printStackTrace();
            return -1;
        }

    }



    @Override
    public void executeTask() {
        Function<Double, Double> function;
        try {
            function = VMResource.lambdaFactory.createLambda(functionString, new TypeReference<Function<Double, Double>>() {
            });
            double result = 0;
            for (int i = 0; i < numberOfIterations; i++) {
                double x = bound.getRandomX();
                result += function.apply(x);
            }
            result *= bound.getDx() / numberOfIterations;

            setResult(new MCResult(this.getTaskId(), VMResource.vmId.get(), result));
            System.out.println(result);
        }
        catch (LambdaCreationException e){
            e.printStackTrace();
        }


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
