package schedulers;

import entities.CloudTask;
import entities.VirtualMachine;

import java.util.*;

public class MinMinScheduler implements BatchScheduler {
    public void bindTasksToVirtualMachines(List<CloudTask> taskList, List<VirtualMachine> vmList) {
        int vmNum = vmList.size();

        Double[] readyTime = new Double[vmNum];
        for (int i = 0; i < readyTime.length; i++) {
            readyTime[i] = 0.0;
        }
        List<List<Double>> tasksVmsMatrix = create2DMatrix(taskList, vmList);
        //initializeTotalTime(tasksVmsMatrix, readyTime);

        int count = 1;
        do {
            System.out.println("==========================");
            System.out.println("This is start of iteration " + count);
            print2DArrayList(tasksVmsMatrix);

            Map<Integer[], Double> map = findMinMinTimeMap(tasksVmsMatrix);
            printMapForMinMin(map);

            Integer[] rowAndColIndexAndTaskId = getRowAndColIndexesAndTaskId(map);

            Double min = getMinimumTimeValue(map);
            int rowIndex = rowAndColIndexAndTaskId[0];
            int columnIndex = rowAndColIndexAndTaskId[1];
            int taskId = rowAndColIndexAndTaskId[2];
            taskList.get(taskId).setVmId(vmList.get(columnIndex).getVmId());
            System.out.println("The task " + taskId + " has been assigned to VM " + columnIndex);
            Double oldReadyTime = readyTime[columnIndex];
            readyTime[columnIndex] = min;
            updateTotalTimeMatrix(columnIndex, oldReadyTime, readyTime, tasksVmsMatrix);
            System.out.println("The ready time array is: " + Arrays.toString(readyTime));
            tasksVmsMatrix.remove(rowIndex);

            System.out.println("This is the end of iteration " + count);
            System.out.println("===========================");
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

    private static List<List<Double>> create2DMatrix(List<CloudTask> taskList, List<VirtualMachine> vmList) {
        List<List<Double>> table = new ArrayList<List<Double>>();
        for (CloudTask aTaskList : taskList) {
            Double originalTaskId = (double) aTaskList.getTaskId();
            List<Double> temp = new ArrayList<Double>();
            for (VirtualMachine aVmList : vmList) {
                Double load = aTaskList.getTaskLength() / aVmList.getMipsValue();
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
