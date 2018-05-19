package schedulers;

import entities.CloudTask;
import entities.VirtualMachine;

import java.util.Queue;
import java.util.Set;

public abstract class OnlineScheduler extends Thread {
    protected Queue<CloudTask> tasks;
    protected Set<VirtualMachine> vms;

    private boolean isEnd;

    public OnlineScheduler(Queue<CloudTask> tasks, Set<VirtualMachine> vms) {
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
