package schedulers;

import entities.CloudTask;
import entities.VirtualMachine;

import java.util.List;

interface BatchScheduler {
    void bindTasksToVirtualMachines(List<CloudTask> taskList, List<VirtualMachine> vmList);
}
