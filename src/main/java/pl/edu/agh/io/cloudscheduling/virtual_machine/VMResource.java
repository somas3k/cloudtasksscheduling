package pl.edu.agh.io.cloudscheduling.virtual_machine;

import com.rabbitmq.client.*;
import pl.edu.agh.io.cloudscheduling.entities.CloudTask;
import pl.edu.agh.io.cloudscheduling.entities.RegisterMessage;
import pl.edu.agh.io.cloudscheduling.utils.MessageUtils;
import pl.edu.agh.io.cloudscheduling.utils.TaskStatus;
import pl.joegreen.lambdaFromString.LambdaFactory;
import pl.joegreen.lambdaFromString.LambdaFactoryConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class VMResource {
    private Consumer consumeTaskFromBroker;
    private Channel registerVMsChannel;
    private Channel dispatcherChannel;
    private String host;
    private String username;
    private String password;
    private String dispatcherExchangeName;
    private String registerExchangeName;
    private String registerKey;
    private String vmKey;
    private String dispatcherKey;
    private String dispatcherQueueName;
    public static AtomicLong vmId;
    public static LambdaFactory lambdaFactory = LambdaFactory.get(LambdaFactoryConfiguration.get().withImports(Math.class));

    public VMResource(String host, String username, String password, String dispatcherExchangeName, String registerExchangeName, String vmKey, String registerKey, String dispatcherKey){
        this.host = host;
        this.username = username;
        this.password = password;
        this.dispatcherExchangeName = dispatcherExchangeName;
        this.registerExchangeName = registerExchangeName;
        this.vmKey = vmKey;
        this.registerKey = registerKey;
        this.dispatcherKey = dispatcherKey;

    }

    public void initializeChannels() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setUsername(this.username);
        factory.setPassword(this.password);

        Connection connection = factory.newConnection();
        registerVMsChannel = connection.createChannel();
        dispatcherChannel = connection.createChannel();
    }

    public void configureChannels() throws Exception {
        registerVMsChannel.exchangeDeclare(registerExchangeName, BuiltinExchangeType.DIRECT);

        dispatcherChannel.exchangeDeclare(dispatcherExchangeName, BuiltinExchangeType.TOPIC);
        dispatcherQueueName = dispatcherChannel.queueDeclare().getQueue();
        dispatcherChannel.queueBind(dispatcherQueueName, dispatcherExchangeName, vmKey);
    }

    public void setupConsumer(){
        consumeTaskFromBroker = new DefaultConsumer(dispatcherChannel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    CloudTask task = (CloudTask) MessageUtils.deserializeMessage(body);
                    task.setStatus(TaskStatus.EXECUTING);
                    System.out.println(task);
                    task.executeTask();
                    sendWithResult(task);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private void sendWithResult(CloudTask task){
        try {
            dispatcherChannel.basicPublish(dispatcherExchangeName, dispatcherKey, null, MessageUtils.serializeMessage(task.getResult()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerInDispatcher(double mips){
        try {
            registerVMsChannel.basicPublish(registerExchangeName, registerKey, null, MessageUtils.serializeMessage(new RegisterMessage(vmKey, mips)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startConsuming(){
        try {
            dispatcherChannel.basicConsume(dispatcherQueueName, true, consumeTaskFromBroker);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        String user = "somas3k";
        String password = "Sabina95";
        String dispatcher = "CTSD";
        String register = "CTSR";
        String vmKey = null;
        String registerKey = "register";
        String dispatcherKey = "broker";



        try {
            URL url = new URL("http://localhost:8090/app/vmId");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            vmKey = in.readLine();
            vmId = new AtomicLong(Long.parseLong(vmKey));
        } catch (IOException e){
            e.printStackTrace();
        }

        VMResource vm = new VMResource(host,user,password,dispatcher,register,vmKey,registerKey, dispatcherKey);
        try {
            vm.initializeChannels();
            vm.configureChannels();
            vm.setupConsumer();
            vm.startConsuming();
            vm.registerInDispatcher(2.5);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
