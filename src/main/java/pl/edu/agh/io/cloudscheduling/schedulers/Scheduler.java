package pl.edu.agh.io.cloudscheduling.schedulers;

import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.VirtualMachine;

import java.util.List;
import java.util.Set;

public abstract class Scheduler extends Thread {

    protected volatile boolean isEnd;

    protected List<CloudTask> tasks;
    protected Set<VirtualMachine> vms;

    public Scheduler(List<CloudTask> tasks, Set<VirtualMachine> vms) {
        this.tasks = tasks;
        this.vms = vms;
        this.isEnd = false;
    }

    public abstract void run();
}
