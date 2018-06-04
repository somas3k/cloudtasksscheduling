package pl.edu.agh.io.cloudscheduling.utils;

import java.util.concurrent.atomic.AtomicLong;

public class IDProvider {
    private static AtomicLong taskIdCounter = new AtomicLong();

    private static AtomicLong vmIdCounter = new AtomicLong();

    public static long getNewTaskId(){
        return taskIdCounter.getAndIncrement();
    }

    public static long getNewVMId(){
        return vmIdCounter.getAndIncrement();
    }
}
