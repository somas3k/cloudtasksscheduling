package schedulers;

import entities.CloudTask;
import entities.VirtualMachine;
import org.apache.commons.math3.linear.*;
import utils.TaskStatus;

import java.util.Queue;
import java.util.Set;

public class PriorityBasedJobScheduler extends OnlineScheduler {
    private CloudTask[] tasksToSchedule;
    private VirtualMachine[] vmsToAllocate;
    private RealMatrix priorityOfResourcesArray;
    private RealMatrix[] priorityOfJobsArrays;


    public PriorityBasedJobScheduler(Queue<CloudTask> tasks, Set<VirtualMachine> vms) {
        super(tasks, vms);
    }

    private void prepareArrays(){
        //tasksToSchedule = tasks.toArray(new CloudTask[0]);
        tasksToSchedule = tasks.stream().filter(task -> task.getStatus().equals(TaskStatus.CREATED) ||task.getStatus().equals(TaskStatus.SCHEDULING)).peek(task -> task.setStatus(TaskStatus.SCHEDULING)).toArray(CloudTask[]::new);
        vmsToAllocate = vms.toArray(new VirtualMachine[0]);
    }

    private void fillPrioritiesOfResources(int vmsNumber){
        double[][] prioritiesOfResources = new double[vmsNumber][vmsNumber];
        for (int i = 0; i < vmsNumber; i++) {
            for (int j = 0; j < vmsNumber; j++) {
                double priorityValue;
                if(i == j){
                    priorityValue = 1;
                }
                else{
                    priorityValue = vmsToAllocate[i].getValueToCalculatePriority() / vmsToAllocate[j].getValueToCalculatePriority();
                }
                prioritiesOfResources[i][j] = priorityValue;

            }
        }
        priorityOfResourcesArray = MatrixUtils.createRealMatrix(prioritiesOfResources);
    }

    private double calculatePriority(double resourcePriority, double priority1, double priority2, double length1, double length2){
        return ((priority1 ) / (priority2 )) * resourcePriority;
    }

    private void fillPriorityOfJobsArrays(int tasksNumber, int vmsNumbers){
        priorityOfJobsArrays = new RealMatrix[vmsNumbers];
        for (int i = 0; i < vmsNumbers; i++) {
            double[][] prioritiesOfJobs = new double[tasksNumber][tasksNumber];
            double resourcePriority = vmsToAllocate[i].getValueToCalculatePriority();
            for (int j = 0; j < tasksNumber; j++) {
                for (int k = 0; k < tasksNumber; k++) {
                    double priorityValue;
                    if (j == k) {
                        priorityValue = 1;
                    }
                    else {
                        priorityValue = calculatePriority(resourcePriority,
                                tasksToSchedule[j].getTaskPriority(),
                                tasksToSchedule[k].getTaskPriority(),
                                tasksToSchedule[j].getTaskLength(),
                                tasksToSchedule[k].getTaskLength());

                    }
                    prioritiesOfJobs[j][k] = priorityValue;
                }
            }
            priorityOfJobsArrays[i] = MatrixUtils.createRealMatrix(prioritiesOfJobs);

        }
    }

    private int getIndexOfMax(double vector[]){
        int index = 0;
        double max = vector[0];
        for (int i = 1; i < vector.length; i++) {
            if(max < vector[i]){
                index = i;
                max = vector[i];
            }
        }
        return index;
    }

    private RealVector[] getPriorityVectors(){
        int count = priorityOfJobsArrays.length;
        RealVector[] PVSs = new RealVector[count];

        for (int i = 0; i < count; i++) {
            EigenDecomposition decomposition = new EigenDecomposition(priorityOfJobsArrays[i]);
            int index = getIndexOfMax(decomposition.getRealEigenvalues());
            PVSs[i] = decomposition.getEigenvector(index);
        }
        return PVSs;
    }

    private RealVector getPriorityVectorOfResources(){
        EigenDecomposition decomposition = new EigenDecomposition(priorityOfResourcesArray);
        int index = getIndexOfMax(decomposition.getRealEigenvalues());
        return decomposition.getEigenvector(index);
    }

    private void fillMatrices(){
        int vmsNumber = vmsToAllocate.length;
        fillPrioritiesOfResources(vmsNumber);
        int tasksNumber = tasksToSchedule.length;
        fillPriorityOfJobsArrays(tasksNumber, vmsNumber);
    }

    @Override
    public void bindTaskWithVM() {
        prepareArrays();
        if(tasksToSchedule.length>0) {
            fillMatrices();
            int vmsNumber = vmsToAllocate.length;
            int tasksNumber = tasksToSchedule.length;
            RealVector priorityVectorOfResource = getPriorityVectorOfResources();
            RealVector[] priorityVectorsOfJobs = getPriorityVectors();
            RealMatrix delta = MatrixUtils.createRealMatrix(vmsNumber, tasksNumber);
            for (int i = 0; i < vmsNumber; i++) {
                delta.setRowVector(i, priorityVectorsOfJobs[i]);
            }
            RealVector PVS = delta.preMultiply(priorityVectorOfResource);

            System.out.println("PVS: ");
            RealVectorFormat format2 = new RealVectorFormat("[", "]", "\t");
            System.out.println(format2.format(PVS));
            System.out.println("Resources: ");
            System.out.println(format2.format(priorityVectorOfResource));
            int taskId = getIndexOfMax(PVS.toArray());
            int vmId = getIndexOfMax(priorityVectorOfResource.toArray());
            vmsToAllocate[vmId].incNumberOfAssignedTasks();
            tasksToSchedule[taskId].setVm(vmsToAllocate[vmId]);
            tasksToSchedule[taskId].setStatus(TaskStatus.WAITING_FOR_SEND);
            System.out.println(vmsToAllocate[vmId]);
            System.out.println(tasksToSchedule[taskId]);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
