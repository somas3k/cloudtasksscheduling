package pl.edu.agh.io.cloudscheduling.schedulers;



import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.VirtualMachine;
import pl.edu.agh.io.cloudscheduling.utils.TaskStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MinMinScheduler extends Scheduler implements BatchScheduler {
    public MinMinScheduler(List<CloudTask> tasks, Set<VirtualMachine> vms) {
        super(tasks, vms);
    }

    private class TaskWrapper{
        final int id;
        private CloudTask task;

        TaskWrapper(int id, CloudTask task) {
            this.id = id;
            this.task = task;
        }

        long getRealId() { return task.getTaskId();}


        long getTaskLength() {
            return task.getTaskLength();
        }

        void setVM(VMWrapper vm){
            task.setVm(vm.vm);
            task.setStatus(TaskStatus.WAITING_FOR_SEND);
        }
    }

    private List<TaskWrapper> wrapTasks(){
        AtomicInteger taskId = new AtomicInteger();
        return tasks.stream().filter(task -> task.getStatus().equals(TaskStatus.CREATED) ||task.getStatus().equals(TaskStatus.SCHEDULING)).peek(task -> task.setStatus(TaskStatus.SCHEDULING)).map(task -> new TaskWrapper(taskId.getAndIncrement(), task)).collect(Collectors.toList());
    }

    private class VMWrapper{
        final int id;
        private VirtualMachine vm;

        VMWrapper(int id, VirtualMachine vm) {
            this.id = id;
            this.vm = vm;
        }

        long getRealId() { return vm.getVmId(); }

        double getMipsValue() {
            return vm.getValueToCalculatePriority();
        }
    }

    private List<VMWrapper> wrapVMs(){
        AtomicInteger taskId = new AtomicInteger();
        return vms.stream().map(vm -> new VMWrapper(taskId.getAndIncrement(), vm)).collect(Collectors.toList());
    }

    @Override
    public void run() {
        while(!isEnd){
            List<TaskWrapper> tasks = wrapTasks();
            List<VMWrapper> vms = wrapVMs();
            if(!(tasks.size() == 0 || vms.size() == 0)){
                bindTasksToVirtualMachines(wrapTasks(), wrapVMs());
            }
        }
    }

    public void bindTasksToVirtualMachines(List<?> taskList, List<?> vmList) {
        int vmNum = vmList.size();


        Double[] readyTime = new Double[vmNum];
        for (int i = 0; i < readyTime.length; i++) {
            readyTime[i] = 0.0;
        }
        List<List<Double>> tasksVmsMatrix = create2DMatrix((List<TaskWrapper>)taskList, (List<VMWrapper>)vmList);
        //initializeTotalTime(tasksVmsMatrix, readyTime);

        int count = 1;
        do {
            //System.out.println("==========================");
            //System.out.println("This is start of iteration " + count);
            //print2DArrayList(tasksVmsMatrix);


            //1. find smallest in each row; and find smallest of all
            Map<Integer[], Double> map = findMinMinTimeMap(tasksVmsMatrix);
            //printMapForMinMin(map);

            //2. retrieve all the info from the map
            Integer[] rowAndColIndexAndTaskId = getRowAndColIndexesAndTaskId(map);

            Double min = getMinimumTimeValue(map);
            int rowIndex = rowAndColIndexAndTaskId[0];
            int columnIndex = rowAndColIndexAndTaskId[1];
            int taskId = rowAndColIndexAndTaskId[2];

            //3. assign the task to the vm based on min-min
            ((TaskWrapper)taskList.get(taskId)).setVM((VMWrapper)vmList.get(columnIndex));
            System.out.println("The task " + ((TaskWrapper)taskList.get(taskId)).getRealId() + " has been assigned to VM " + ((VMWrapper)vmList.get(columnIndex)).getRealId());

            //4. update ready-time array
            Double oldReadyTime = readyTime[columnIndex];
            readyTime[columnIndex] = min;

            //5. update task-vm matrix with the current ready-time
            updateTotalTimeMatrix(columnIndex, oldReadyTime, readyTime, tasksVmsMatrix);
            //System.out.println("The ready time array is: " + Arrays.toString(readyTime));

            //6. remove the row after the task has been assigned to vm
            tasksVmsMatrix.remove(rowIndex);

            //System.out.println("This is the end of iteration " + count);
            //System.out.println("===========================");
            ++count;
        }
        while (tasksVmsMatrix.size() > 0);
    }

    private static void printMapForMinMin(Map<Integer[],Double> map) {
        for(Map.Entry<Integer[], Double> entry : map.entrySet()){
            Integer[] key = entry.getKey();
            Double value = entry.getValue();
            System.out.printf("The keys are: { %d, %d, %d }", key[0], key[1], key[2]);
            System.out.println("Min: " + value);
        }

    }

    private static void print2DArrayList(List<List<Double>> tasksVmsMatrix) {
        System.out.printf("The current matrix is as below, with size of %d by %d \n", tasksVmsMatrix.size(),
                tasksVmsMatrix.get(0).size());
        for (List<Double> aTasksVmsMatrix : tasksVmsMatrix) {
            for (Double anATasksVmsMatrix : aTasksVmsMatrix) {
                System.out.printf("%-11.5f", anATasksVmsMatrix);
            }
            System.out.println();
        }
    }

    private static Double getMinimumTimeValue(Map<Integer[],Double> map) {
        Double value = 0.0;
        for(Map.Entry<Integer[], Double> entry : map.entrySet()){
            value = entry.getValue();
        }
        return value;
    }


    private static Integer[] getRowAndColIndexesAndTaskId(Map<Integer[],Double> map) {
        Integer[] key = new Integer[3];
        for(Map.Entry<Integer[], Double> entry : map.entrySet()){
            key = entry.getKey();
        }
        return key;
    }

    private static void updateTotalTimeMatrix(int columnIndex, Double oldReadyTime, Double[] readyTime,
                                              List<List<Double>> tasksVmsMatrix){
        Double newReadyTime = readyTime[columnIndex];
        Double readyTimeDifference = newReadyTime - oldReadyTime;
        for (List<Double> aTasksVmsMatrix : tasksVmsMatrix) {
            Double oldTotalTime = aTasksVmsMatrix.get(columnIndex);
            Double newTotalTime = oldTotalTime + readyTimeDifference;
            aTasksVmsMatrix.set(columnIndex, newTotalTime);
        }

    }

    private static Map<Integer[],Double> findMinMinTimeMap(List<List<Double>> tasksVmsMatrix) {
        List<Integer[]> indexList = new ArrayList<Integer[]>();
        int colNum = tasksVmsMatrix.get(0).size();
        Integer initialTaskId = tasksVmsMatrix.get(0).get(colNum - 1).intValue();
        Integer[] indexOfMin = {0, 0, initialTaskId};
        Double min = tasksVmsMatrix.get(0).get(0);
        indexList.add(indexOfMin);
        int rowNum = tasksVmsMatrix.size();
        for (int row = 0; row < rowNum; row++) {
            int colNumWithoutLastColumn = tasksVmsMatrix.get(row).size() - 1;
            for (int col = 0; col < colNumWithoutLastColumn; col++) {
                Double current = tasksVmsMatrix.get(row).get(col);
                if(current < min){
                    min = current;
                    Integer targetTaskId = tasksVmsMatrix.get(row).get(colNumWithoutLastColumn).intValue();
                    Integer[] indexOfCurrent = {row, col, targetTaskId};
                    indexList.add(indexOfCurrent);
                }
            }
        }
        Map<Integer[], Double> map = new HashMap<Integer[], Double>();
        Integer[] rowAndColIndexAndTaskId = new Integer[3];
        rowAndColIndexAndTaskId[0] = indexList.get(indexList.size() -1)[0];
        rowAndColIndexAndTaskId[1] = indexList.get(indexList.size() -1)[1];
        rowAndColIndexAndTaskId[2] = indexList.get(indexList.size() -1)[2];

        map.put(rowAndColIndexAndTaskId, min);

        return map;

    }

    private static List<List<Double>> create2DMatrix(List<TaskWrapper> taskList, List<VMWrapper> vmList) {
        List<List<Double>> table = new ArrayList<List<Double>>();
        for (TaskWrapper aTaskList : taskList) {
            Double originalTaskId = (double) aTaskList.id;
            List<Double> temp = new ArrayList<Double>();
            for (VMWrapper aVmList : vmList) {
                Double load = (double)aTaskList.getTaskLength() / aVmList.getMipsValue();
                temp.add(load);
            }
            temp.add(originalTaskId);
            table.add(temp);
        }
        return table;
    }

    private static void initializeTotalTime(List<List<Double>> tasksVmsMatrix, Double[] readyTimeArray){
        for (int i = 0; i < tasksVmsMatrix.size(); i++) {
            List<Double> temp = new ArrayList<Double>();
            for (int j = 0; j < readyTimeArray.length; j++) {
                Double readyTime = readyTimeArray[j];
                Double currentCompletionTime = tasksVmsMatrix.get(i).get(j);
                currentCompletionTime += readyTime;
                temp.add(currentCompletionTime);
            }
            tasksVmsMatrix.set(i, temp);
        }
    }

    public double calculateAvgTurnAroundTime(List<CloudTask> taskList){
        double totalTime = 0.0;
        int taskNum = taskList.size();
        for (CloudTask aTaskList : taskList) {
            totalTime += aTaskList.getFinishTime();
        }
        double averageTurnAroundTime = totalTime / taskNum;
        System.out.println("The average turnaround time is " + averageTurnAroundTime);
        return averageTurnAroundTime;
    }

    public double calculateThroughput(List<CloudTask> taskList){
        double maxFinishTime = 0.0;
        int taskNum = taskList.size();
        for (CloudTask aTaskList : taskList) {
            double currentFinishTime = aTaskList.getFinishTime();
            if (currentFinishTime > maxFinishTime) {
                maxFinishTime = currentFinishTime;
            }
        }
        double throughput = taskNum / maxFinishTime;
        System.out.println("The throughput is " + throughput);
        return throughput;
    }
}
