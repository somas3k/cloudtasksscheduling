package pl.edu.agh.io.cloudscheduling.broker;

import com.rabbitmq.client.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.io.cloudscheduling.entities.CloudResult;
import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.RegisterMessage;
import pl.edu.agh.io.cloudscheduling.entities.VirtualMachine;
import pl.edu.agh.io.cloudscheduling.schedulers.OnlineScheduler;
import pl.edu.agh.io.cloudscheduling.schedulers.PriorityBasedJobScheduler;
import pl.edu.agh.io.cloudscheduling.schedulers.Scheduler;
import pl.edu.agh.io.cloudscheduling.tasks.MonteCarloTask;
import pl.edu.agh.io.cloudscheduling.utils.MessageUtils;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class Broker {
    private Consumer registerVMsConsumer;
    private Consumer consumeResultFromVM;
    private Channel registerVMsChannel;
    private Channel dispatcherChannel;
    private String host;
    private String username;
    private String password;
    private String dispatcherExchangeName;
    private String registerExchangeName;
    private String registerKey;
    private String dispatcherKey;
    private String dispatcherQueueName;
    private String registerQueueName;
    private Set<VirtualMachine> vms;
    private List<CloudTask> tasks;
    private TaskDispatcher taskDispatcher;
    private Scheduler scheduler;

    public Broker(@Value("${broker.host}")String host, @Value("${broker.username}")String username,
                  @Value("${broker.password}")String password, @Value("${broker.dispatcherExchangeName}")String dispatcherExchangeName,
                  @Value("${broker.registerExchangeName}")String registerExchangeName,
                  @Value("${broker.dispatcherKey}")String dispatcherKey, @Value("${broker.registerKey}")String registerKey){
        this.host = host;
        this.username = username;
        this.password = password;
        this.dispatcherExchangeName = dispatcherExchangeName;
        this.registerExchangeName = registerExchangeName;
        this.dispatcherKey = dispatcherKey;
        this.registerKey = registerKey;

        this.vms = new ConcurrentSkipListSet<>();
        this.tasks = new CopyOnWriteArrayList<>();
        this.scheduler = new PriorityBasedJobScheduler(tasks, vms);

        try {
            this.initializeChannels();
            this.configureChannels();
            this.setupConsumers();
            this.startConsuming();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void initializeChannels() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setUsername(this.username);
        factory.setPassword(this.password);

            Connection connection = factory.newConnection();
        registerVMsChannel = connection.createChannel();
        dispatcherChannel = connection.createChannel();
        this.taskDispatcher = new TaskDispatcher(tasks, dispatcherChannel, dispatcherExchangeName);
    }

    public void configureChannels() throws Exception {
        registerVMsChannel.exchangeDeclare(registerExchangeName, BuiltinExchangeType.DIRECT);
        registerQueueName = registerVMsChannel.queueDeclare().getQueue();
        registerVMsChannel.queueBind(registerQueueName, registerExchangeName, registerKey);

        dispatcherChannel.exchangeDeclare(dispatcherExchangeName, BuiltinExchangeType.TOPIC);
        dispatcherQueueName = dispatcherChannel.queueDeclare().getQueue();
        dispatcherChannel.queueBind(dispatcherQueueName, dispatcherExchangeName, dispatcherKey);
    }

    private void setupConsumers(){
        registerVMsConsumer = new DefaultConsumer(registerVMsChannel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    RegisterMessage message = (RegisterMessage)MessageUtils.deserializeMessage(body);
                    registerVM(message);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        consumeResultFromVM = new DefaultConsumer(dispatcherChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    CloudResult result = (CloudResult)MessageUtils.deserializeMessage(body);
                    handleResult(result);

                }
                catch (ClassNotFoundException e){
                    e.printStackTrace();
                }
            }
        };
    }

    private void handleResult(CloudResult result){
        CloudTask t = tasks.stream().filter(task -> task.getTaskId()==result.getTaskId()).findAny().get();
        t.getResponseResult().setResult(ResponseEntity.ok(result));
        tasks.remove(t);
        vms.stream().filter(vm -> vm.getVmId()==result.getVmId()).findAny().get().decNumberOfAssignedTasks();

    }

    private void registerVM(RegisterMessage message){
        if(vms.size() == 0) {
            vms.add(new VirtualMachine(vms.size(), message.getMipsValue(), message.getKey()));
            System.out.println("VM added " + message.getKey());
            startScheduler();
            startDispatcher();
        }
        else{
            vms.add(new VirtualMachine(vms.size(), message.getMipsValue(), message.getKey()));
            System.out.println("VM added " + message.getKey());
        }
    }

    public void startConsuming(){
        try {
            registerVMsChannel.basicConsume(registerQueueName, true, registerVMsConsumer);
            dispatcherChannel.basicConsume(dispatcherQueueName, true, consumeResultFromVM);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDispatcher(){
        taskDispatcher.start();
    }

    private void startScheduler(){
        scheduler.start();
    }

    public void setTasks(List<CloudTask> tasks){
        this.tasks.addAll(tasks);
    }

    private static void setExampleTasks(Broker broker){
        MonteCarloTask t1 = new MonteCarloTask(0, 300000, 5, x -> x*x, -1, 1);
        MonteCarloTask t2 = new MonteCarloTask(1, 500000, 6, x -> Math.sqrt(x) + 2, 0, 2);
        MonteCarloTask t3 = new MonteCarloTask(2, 10000000, 1, x -> 2*x + 5, -10, 15);
        MonteCarloTask t4 = new MonteCarloTask(3, 20, 6, x -> 1d, -1, 1);
        broker.setTasks(Arrays.asList(t1, t2, t3, t4));
    }

    public void addTask(CloudTask task){
        this.tasks.add(task);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDispatcherExchangeName(String dispatcherExchangeName) {
        this.dispatcherExchangeName = dispatcherExchangeName;
    }

    public void setRegisterExchangeName(String registerExchangeName) {
        this.registerExchangeName = registerExchangeName;
    }

    public void setRegisterKey(String registerKey) {
        this.registerKey = registerKey;
    }

    public void setDispatcherKey(String dispatcherKey) {
        this.dispatcherKey = dispatcherKey;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDispatcherExchangeName() {
        return dispatcherExchangeName;
    }

    public String getRegisterExchangeName() {
        return registerExchangeName;
    }

    public String getRegisterKey() {
        return registerKey;
    }

    public String getDispatcherKey() {
        return dispatcherKey;
    }
}
