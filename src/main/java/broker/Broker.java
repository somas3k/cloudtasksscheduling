package broker;

import com.rabbitmq.client.*;
import entities.CloudTask;
import entities.RegisterMessage;
import entities.VirtualMachine;
import schedulers.OnlineScheduler;
import schedulers.PriorityBasedJobScheduler;
import tasks.MonteCarloTask;
import utils.MessageUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

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
    private Queue<CloudTask> tasks;
    private TaskDispatcher taskDispatcher;
    private OnlineScheduler scheduler;

    public Broker(String host, String username, String password, String dispatcherExchangeName, String registerExchangeName, String dispatcherKey, String registerKey){
        this.host = host;
        this.username = username;
        this.password = password;
        this.dispatcherExchangeName = dispatcherExchangeName;
        this.registerExchangeName = registerExchangeName;
        this.dispatcherKey = dispatcherKey;
        this.registerKey = registerKey;
        this.vms = new ConcurrentSkipListSet<>();
        this.tasks = new ConcurrentLinkedQueue<>();
        this.scheduler = new PriorityBasedJobScheduler(tasks, vms);
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
        broker.setTasks(Arrays.asList(t1, t2, t3));
    }

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        String host = "localhost";
        String user = "somas3k";
        String password = "Sabina95";
        String dispatcher = "CTSD";
        String register = "CTSR";
        String dispatcherKey = "broker";
        String registerKey = "register";


        Broker broker = new Broker(host,user,password,dispatcher,register,dispatcherKey,registerKey);
        setExampleTasks(broker);
        try {
            broker.initializeChannels();
            broker.configureChannels();
            broker.setupConsumers();
            broker.startConsuming();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
