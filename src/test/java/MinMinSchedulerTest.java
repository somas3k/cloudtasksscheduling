import pl.edu.agh.io.cloudscheduling.entities.*;
import pl.edu.agh.io.cloudscheduling.schedulers.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


class MinMinSchedulerTest {

    private List<CloudTask> prepareCloudTaskList(){
        CloudTask ct1 = new CloudTask(0, -1) {
            @Override
            public void executeTask() {
            }
        };
        ct1.setTaskLength(1500);
        CloudTask ct2 = new CloudTask(1, -1) {
            @Override
            public void executeTask() {
            }
        };
        ct2.setTaskLength(2000);
        CloudTask ct3 = new CloudTask(2, -1) {
            @Override
            public void executeTask() {
            }
        };
        ct3.setTaskLength(500);
        CloudTask ct4 = new CloudTask(3, -1) {
            @Override
            public void executeTask() {

            }
        };
        ct4.setTaskLength(2500);
        List<CloudTask> taskList = new ArrayList<>();
        taskList.add(ct1);
        taskList.add(ct2);
        taskList.add(ct3);
        taskList.add(ct4);
        return taskList;
    }

    private List<VirtualMachine> prepareVMList(){
        VirtualMachine vm1 = new VirtualMachine(0, 19, "a");
        VirtualMachine vm2 = new VirtualMachine(1, 20, "b");
        VirtualMachine vm3 = new VirtualMachine(2, 20, "c");
        VirtualMachine vm4 = new VirtualMachine(3, 21, "d");
        List<VirtualMachine>  vmList = new ArrayList<>();
        vmList.add(vm1);
        vmList.add(vm2);
        vmList.add(vm3);
        vmList.add(vm4);
        return vmList;
    }

    @Test
    void shouldScheduleCorrectly() {
        //given
        List<CloudTask> taskList = prepareCloudTaskList();
        List<VirtualMachine> vmList = prepareVMList();
        BatchScheduler scheduler = new MinMinScheduler(taskList, new HashSet<>(vmList));

        //when
        scheduler.bindTasksToVirtualMachines(taskList, vmList);

        //then
        assertEquals(1, taskList.get(0).getVm().getVmId());
        assertEquals(2, taskList.get(1).getVm().getVmId());
        assertEquals(3, taskList.get(2).getVm().getVmId());
        assertEquals(0, taskList.get(3).getVm().getVmId());
    }


}
