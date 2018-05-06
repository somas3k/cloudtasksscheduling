import entities.*;
import schedulers.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;


class MinMinSchedulerTest {

    private List<CloudTask> prepareCloudTaskList(){
        CloudTask ct1 = new CloudTask(0, -1, 1500) {
            @Override
            public void executeTask() {
            }
        };
        CloudTask ct2 = new CloudTask(1, -1, 2000) {
            @Override
            public void executeTask() {
            }
        };
        CloudTask ct3 = new CloudTask(2, -1, 500) {
            @Override
            public void executeTask() {
            }
        };
        CloudTask ct4 = new CloudTask(3, -1, 2500) {
            @Override
            public void executeTask() {

            }
        };
        List<CloudTask> taskList = new ArrayList<>();
        taskList.add(ct1);
        taskList.add(ct2);
        taskList.add(ct3);
        taskList.add(ct4);
        return taskList;
    }

    private List<VirtualMachine> prepareVMList(){
        VirtualMachine vm1 = new VirtualMachine(0, 19);
        VirtualMachine vm2 = new VirtualMachine(1, 20);
        VirtualMachine vm3 = new VirtualMachine(2, 20);
        VirtualMachine vm4 = new VirtualMachine(3, 21);
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
        BatchScheduler scheduler = new MinMinScheduler();

        //when
        scheduler.bindTasksToVirtualMachines(taskList, vmList);

        //then
        assertEquals(1, taskList.get(0).getVmId());
        assertEquals(2, taskList.get(1).getVmId());
        assertEquals(3, taskList.get(2).getVmId());
        assertEquals(0, taskList.get(3).getVmId());
    }

}
