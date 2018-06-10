package pl.edu.agh.io.cloudscheduling.schedulers;



import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.VirtualMachine;

import java.util.List;

public interface BatchScheduler {
    void bindTasksToVirtualMachines(List<?> taskList, List<?> vmList);
}
