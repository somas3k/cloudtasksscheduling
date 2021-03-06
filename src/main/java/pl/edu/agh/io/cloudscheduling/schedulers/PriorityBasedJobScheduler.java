package pl.edu.agh.io.cloudscheduling.schedulers;


import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.linear.*;
import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.VirtualMachine;
import pl.edu.agh.io.cloudscheduling.utils.TaskStatus;


import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityBasedJobScheduler extends OnlineScheduler {
    private CloudTask[] tasksToSchedule;
    private VirtualMachine[] vmsToAllocate;
    private RealMatrix priorityOfResourcesArray;
    private RealMatrix[] priorityOfJobsArrays;


    public PriorityBasedJobScheduler(List<CloudTask> tasks, Set<VirtualMachine> vms) {
        super(tasks, vms);
    }

    private void prepareArrays(){
        //tasksToSchedule = tasks.toArray(new CloudTask[0]);
        vmsToAllocate = vms.toArray(new VirtualMachine[0]);

        tasksToSchedule = tasks.stream().filter(task -> task.getStatus().equals(TaskStatus.CREATED) ||task.getStatus().equals(TaskStatus.SCHEDULING)).peek(task -> task.setStatus(TaskStatus.SCHEDULING)).toArray(CloudTask[]::new);
        if(tasksToSchedule.length > vmsToAllocate.length*10) {
            AtomicInteger counter = new AtomicInteger();

            tasksToSchedule = Arrays.stream(tasksToSchedule).filter(task -> counter.incrementAndGet() < vmsToAllocate.length*10).toArray(CloudTask[]::new);
        }
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
            try {
                EigenDecomposition decomposition = new EigenDecomposition(priorityOfJobsArrays[i]);
                int index = getIndexOfMax(decomposition.getRealEigenvalues());
                PVSs[i] = decomposition.getEigenvector(index);
            }
            catch (MaxCountExceededException e) {
                System.out.println("blad2");
                return null;}

        }
        return PVSs;
    }

    private RealVector getPriorityVectorOfResources(){
        try {
            EigenDecomposition decomposition = new EigenDecomposition(priorityOfResourcesArray);
            int index = getIndexOfMax(decomposition.getRealEigenvalues());
            return decomposition.getEigenvector(index);
        }
        catch (MaxCountExceededException e) {
            System.out.println("blad");
            return null;}

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
            if(priorityVectorOfResource != null) {
                RealVector[] priorityVectorsOfJobs = getPriorityVectors();
                if(priorityVectorsOfJobs != null) {
                    RealMatrix delta = MatrixUtils.createRealMatrix(vmsNumber, tasksNumber);
                    for (int i = 0; i < vmsNumber; i++) {
                        delta.setRowVector(i, priorityVectorsOfJobs[i]);
                    }
                    RealVector PVS = delta.preMultiply(priorityVectorOfResource);

                    //System.out.println("PVS: ");
                    //RealVectorFormat format2 = new RealVectorFormat("[", "]", "\t");
                    //System.out.println(format2.format(PVS));
                    //System.out.println("Resources: ");
                    //System.out.println(format2.format(priorityVectorOfResource));
                    int taskId = getIndexOfMax(PVS.toArray());
                    int vmId = getIndexOfMax(priorityVectorOfResource.toArray());
                    vmsToAllocate[vmId].incNumberOfAssignedTasks();
                    tasksToSchedule[taskId].setVm(vmsToAllocate[vmId]);
                    tasksToSchedule[taskId].setStatus(TaskStatus.WAITING_FOR_SEND);
                    //System.out.println(vmsToAllocate[vmId]);
                    //System.out.println(tasksToSchedule[taskId]);
                }
            }
        }
        else try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
