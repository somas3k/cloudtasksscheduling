package schedulers;

import entities.CloudTask;
import entities.VirtualMachine;

import java.util.Set;

public abstract class OnlineScheduler implements Runnable {
    protected Set<CloudTask> tasks;
    protected Set<VirtualMachine> vms;

    private boolean isEnd;

    public OnlineScheduler(Set<CloudTask> tasks, Set<VirtualMachine> vms) {
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
