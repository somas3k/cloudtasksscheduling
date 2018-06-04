package pl.edu.agh.io.cloudscheduling.schedulers;

import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.VirtualMachine;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class OnlineScheduler extends Scheduler {

    private boolean isEnd;

    public OnlineScheduler(List<CloudTask> tasks, Set<VirtualMachine> vms) {
        super(tasks, vms);
        this.tasks = tasks;
        this.vms = vms;
        this.isEnd = false;
    }

    public abstract void bindTaskWithVM();

    @Override
    public void run() {
        while(!isEnd){
            bindTaskWithVM();
        }
    }

    public synchronized void closeScheduler(){
        this.isEnd = true;
    }

}
