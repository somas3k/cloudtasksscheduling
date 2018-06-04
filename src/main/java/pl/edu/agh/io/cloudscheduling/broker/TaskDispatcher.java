package pl.edu.agh.io.cloudscheduling.broker;

import com.rabbitmq.client.Channel;
import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.utils.MessageUtils;
import pl.edu.agh.io.cloudscheduling.utils.TaskStatus;


import java.io.IOException;
import java.util.List;

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
        while(!stop){
            tasks.stream().filter(task -> task.getStatus().equals(TaskStatus.WAITING_FOR_SEND)).forEach(task -> {
                try {
                    dispatcherChannel.basicPublish(exchangeName, task.getVm().getKey(), null, MessageUtils.serializeMessage(task));
                    task.setStatus(TaskStatus.EXECUTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public synchronized void stopLoop(){
        stop = true;
    }
}