package pl.edu.agh.io.cloudscheduling.broker;

import com.rabbitmq.client.Channel;
import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.utils.MessageUtils;
import pl.edu.agh.io.cloudscheduling.utils.TaskStatus;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TaskDispatcher extends Thread {
    private List<CloudTask> tasks;
    private Channel dispatcherChannel;
    private String exchangeName;


    private volatile boolean stop = false;

    public TaskDispatcher(List<CloudTask> tasks, Channel dispatcherChannel, String exchangeName) {
        super();
        this.tasks = tasks;
        this.dispatcherChannel = dispatcherChannel;
        this.exchangeName = exchangeName;
    }

    @Override
    public void run() {
        AtomicLong schedulingTime = new AtomicLong();
        AtomicInteger counter = new AtomicInteger();
        while(!stop){
            tasks.stream().filter(task -> task.getStatus().equals(TaskStatus.WAITING_FOR_SEND)).forEach(task -> {
                try {
                    dispatcherChannel.basicPublish(exchangeName, task.getVm().getKey(), null, MessageUtils.serializeMessage(task));
                    task.setStatus(TaskStatus.EXECUTING);
                    schedulingTime.addAndGet(task.getSchedulingTime());
                    counter.incrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            if(counter.get() == 100) {
                System.out.println("==============Scheduling time: " + schedulingTime + " =======================");
                counter.set(0);
            }
        }
    }

    public synchronized void stopLoop(){
        stop = true;
    }
}
