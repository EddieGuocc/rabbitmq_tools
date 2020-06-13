package com.gcy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gcy.message.Message;
import com.gcy.message.MessageWorker;
import com.gcy.queue.Queue;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/*
 * @Author gcy
 * @Description 主要工具类
 * @Date 16:34 2020/6/12
 * @Param
 * @return
 **/
public final class MQUtils {
    private static String mqUser;
    private static String mqPassword;
    private static String mqVhost;
    private static List<Address> clusterAddress = new ArrayList<>();
    private static AtomicBoolean factoryInited = new AtomicBoolean(false);
    private static ObjectMapper mapper = new ObjectMapper();

    private static MQPublish[] mqPublishers = null;
    private static MQConsumer[] mqConsumers = null;

    private static ConnectionFactory factory = null;


    /*
     * @Author gcy
     * @Description 配置信息获取
     * @Date 17:22 2020/6/12
     * @Param
     * @return
     **/
    static {
        Properties properties = new Properties();
        try {
            properties.load(MQUtils.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mqUser = properties.getProperty("mq.host.username");
        mqPassword = properties.getProperty("mq.host.password");
        mqVhost = properties.getProperty("mq.host.vhost");

        String[] clusterServer = properties.getProperty("mq.cluster.server").split(",");
        for(int i = 0; i < clusterServer.length; i++){
            String[] server_item = clusterServer[i].split(":");
            clusterAddress.add(new Address(server_item[0], Integer.parseInt(server_item[1])));
        }

        System.out.println("配置信息加载完毕[user=" + mqUser + ", pwd="+ mqPassword +  ", clusterServer=" + clusterServer + ", vhost="
                + mqVhost + "]");

    }

    /*
     * @Author gcy
     * @Description 连接信息初始化
     * @Date 17:22 2020/6/12
     * @Param []
     * @return boolean
     **/
    private static boolean init(){
        if(!factoryInited.get()){
            synchronized (factoryInited){
                if(!factoryInited.get()){
                    System.out.println("连接信息初始化");
                    mapper.configure(SerializationFeature.INDENT_OUTPUT, Boolean.TRUE );
                    //在反序列化时，忽略目标对象没有的属性。凡是使用该objectMapper反序列化时，都会拥有该特性。
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
                    factory = new ConnectionFactory();
                    factory.setUsername(mqUser);
                    factory.setPassword(mqPassword);
                    factory.setVirtualHost(mqVhost);

                    factory.setAutomaticRecoveryEnabled(true);
                    factory.setTopologyRecoveryEnabled(true);

                    mqPublishers = new MQPublish[Queue.values().length];
                    mqConsumers = new MQConsumer[Queue.values().length];

                    for (int i = 0; i < Queue.values().length; i++) {
                        mqPublishers[i] = null;
                        mqConsumers[i] = null;
                    }

                    factoryInited.set(true);

                    System.out.println("初始化配置信息...");
                }
            }
        }

        return true;
    }

    /*
     * @Author gcy
     * @Description 关闭连接
     * @Date 17:22 2020/6/12
     * @Param []
     * @return void
     **/
    public static void close() {
        if (mqPublishers != null) {
            for (int i = 0; i < mqPublishers.length; i++) {
                if (mqPublishers[i] != null) {
                    mqPublishers[i].close();
                    mqPublishers[i] = null;
                }
            }
        }

        if (mqConsumers != null) {
            for (int i = 0; i < mqConsumers.length; i++) {
                if (mqConsumers[i] != null) {
                    mqConsumers[i].close();
                    mqConsumers[i] = null;
                }
            }
        }

        factoryInited.set(false);
        System.out.println("关闭所有连接...");
    }

    /*
     * @Author gcy
     * @Description 注入当前worker
     * @Date 17:05 2020/6/12
     * @Param [queue, msg]
     * @return boolean
     **/
    public static boolean publish(Queue queue, Message msg) {
        if (init()) {
            if (mqPublishers[queue.ordinal()] == null) {
                synchronized (mqPublishers) {
                    if (mqPublishers[queue.ordinal()] == null) {
                        mqPublishers[queue.ordinal()] = new MQPublish(queue);
                    }
                }
            }
        }
        return mqPublishers[queue.ordinal()].publish(msg);
    }

    /*
     * @Author gcy
     * @Description 注入当前worker 携带级别
     * @Date 17:12 2020/6/12
     * @Param [queue, msg, queueMaxLev, queueLev]
     * @return boolean
     **/
    public static boolean publish(Queue queue, Message msg,int queueMaxLev,int queueLev) {
        if (init()) {
            if (mqPublishers[queue.ordinal()] == null) {
                synchronized (mqPublishers) {
                    if (mqPublishers[queue.ordinal()] == null) {
                        mqPublishers[queue.ordinal()] = new MQPublish(queue,queueMaxLev);
                    }
                }
            }
        }
        return mqPublishers[queue.ordinal()].publishLevel(msg,queueLev);
    }

    /*
     * @Author gcy
     * @Description 绑定监听worker
     * @Date 17:18 2020/6/12
     * @Param [queue, worker]
     * @return com.gcy.MQUtils.MQConsumer<T>
     **/
    public static <T extends Message> MQConsumer<T> bind(Queue queue, MessageWorker<T> worker) {
        if (init()) {
            if (mqConsumers[queue.ordinal()] == null) {
                synchronized (mqConsumers) {
                    if (mqConsumers[queue.ordinal()] == null) {
                        mqConsumers[queue.ordinal()] = new MQConsumer<T>(queue);
                    }
                }
            }
        }
        return mqConsumers[queue.ordinal()].bind(worker);
    }

    /*
     * @Author gcy
     * @Description 绑定监听worker(携带优先级)
     * @Date 17:18 2020/6/12
     * @Param [queue, worker, queueMaxLev]
     * @return com.gcy.MQUtils.MQConsumer<T>
     **/
    public static <T extends Message> MQConsumer<T> bind(Queue queue, MessageWorker<T> worker,int queueMaxLev) {
        if (init()) {
            if (mqConsumers[queue.ordinal()] == null) {
                synchronized (mqConsumers) {
                    if (mqConsumers[queue.ordinal()] == null) {
                        mqConsumers[queue.ordinal()] = new MQConsumer<T>(queue,queueMaxLev);
                    }
                }
            }
        }
        return mqConsumers[queue.ordinal()].bind(worker);
    }


    /*
     * @Author gcy
     * @Description 建立连接父类
     * @Date 17:32 2020/6/12
     * @Param
     * @return
     **/
    private static class MQDriver {
        protected String queueName = null;
        protected Connection connection = null;
        protected Channel channel = null;
        protected int queueMaxLev;
        protected AtomicBoolean inited = new AtomicBoolean(false);
        protected boolean durable = false;

        public MQDriver(Queue queue, int queueMaxLev, boolean durable) {
            this.queueName = queue.name();
            this.queueMaxLev = queueMaxLev;
            this.durable = durable;
        }

        protected boolean init(){
            if(!this.inited.get()){
                synchronized (this.inited){
                    if(!this.inited.get()){
                        close();
                        try {
                            this.connection = factory.newConnection(clusterAddress);
                            this.channel = this.connection.createChannel();
                            Map<String, Object> dic = new HashMap<>();
                            //声明队列时进行判断 添加队列优先级
                            if(QueueLevel.LOWEST.getLevel() == this.queueMaxLev){
                                dic = null;
                            }else {
                                dic.put("x-max-priority", this.queueMaxLev);
                            }
                            /*声明队列
                             * 参数1 队列名
                             * 参数2 是否持久化（true 重启后队列仍存在）
                             * 参数3 独享队列，仅对首次声明Connection可见，断开自动删除
                             * 参数4 自动删除，在没有消费者连接的时候，自动删除该队列
                             * 参数5 其他参数
                             **/
                            this.channel.queueDeclare(this.queueName, this.durable, false, false, dic);
                            this.inited.set(true);
                            return true;

                        } catch (Throwable e) {
                            e.printStackTrace();
                            close();
                            this.inited.set(false);
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        /*
         * @Author gcy
         * @Description 关闭连接
         * @Date 14:29 2020/6/13
         * @Param []
         * @return void
         **/
        protected void close(){
            if(this.channel != null){
                try {
                    this.channel.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }finally {
                    this.channel = null;
                }
            }
            if(this.connection != null){
                try {
                    this.connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    this.connection = null;
                }
            }
        }

        protected long getMessageCount() {
            try {
                return this.channel.messageCount(this.queueName);
            } catch (IOException e) {
                e.printStackTrace();
                return -1L;
            }
        }
    }

    /*
     * @Author gcy
     * @Description 生产者
     * @Date 17:32 2020/6/12
     * @Param
     * @return
     **/
    private static class MQPublish extends MQDriver{

        public MQPublish(Queue queue) {
            super(queue, QueueLevel.LOWEST.getLevel(), true);
        }

        public MQPublish(Queue queue, int queueMaxLev) {
            super(queue, queueMaxLev, true);
        }

        public MQPublish(Queue queue, Boolean durable) {
            super(queue, QueueLevel.LOWEST.getLevel(), durable);
        }

        public MQPublish(Queue queue, int queueMaxLev, boolean durable) {
            super(queue, queueMaxLev, durable);
        }

        /*
         * @Author gcy
         * @Description 发送消息核心方法
         * @Date 17:15 2020/6/12
         * @Param [message]
         * @return boolean
         **/
        private boolean publish(Message message){
            if(init()){
                try {
                    //单独投递某队列，绑定到默认交换机上（default exchange），routingKey与队列名一致
                    this.channel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, mapper.writeValueAsBytes(message));
                    //指定交换机和routingKey投递
                    this.channel.basicPublish("1", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, mapper.writeValueAsBytes(message));
                    System.out.println("向"+ this.queueName + "发送消息");
                    return true;
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.out.println("向"+ this.queueName + "发送消息失败");
                    close();
                    this.inited.set(false);
                    return false;
                }
            }
            System.out.println("向"+ this.queueName + "发送消息失败");
            return false;
        }

        /*
         * @Author gcy
         * @Description 发送消息核心方法（携带优先级）
         * @Date 17:16 2020/6/12
         * @Param [message, queueLev]
         * @return boolean
         **/
        private boolean publishLevel(Message message, int queueLev){
            if(init()){
                try {
                    AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
                    builder.contentType("text/plain");
                    builder.priority(queueLev);
                    AMQP.BasicProperties properties = builder.build();

                    this.channel.basicPublish("", this.queueName, properties, mapper.writeValueAsBytes(message));
                    System.out.println("向"+ this.queueName + "发送消息成功");
                    return true;
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.out.println("向"+ this.queueName + "发送消息失败");
                    close();
                    this.inited.set(false);
                    return false;
                }
            }
            System.out.println("向"+ this.queueName + "发送消息失败");
            return false;
        }
    }


    /*
     * @Author gcy
     * @Description 消费者
     * @Date 17:32 2020/6/12
     * @Param
     * @return
     **/
    public static class MQConsumer<T extends Message> extends MQDriver{

        private boolean stop = false;

        private List<MessageWorker<T>> workers = new ArrayList<MessageWorker<T>>(1);

        public MQConsumer(Queue queue, int queueMaxLev) {
            super(queue, queueMaxLev, true);
        }

        public MQConsumer(Queue queue) {
            super(queue,QueueLevel.LOWEST.getLevel(),true);
        }

        public MQConsumer(Queue queue,Boolean durable) {
            super(queue,QueueLevel.LOWEST.getLevel(),durable);
        }

        public MQConsumer(Queue queue,int queueLev,Boolean durable) {
            super(queue,queueLev,durable);
        }

        @Override
        protected boolean init(){
            //父类加载 建立连接进行初始化
            if(super.init()){
                try {
                    //不要一次性给消费者推送超过【参数i】个的消息
                    this.channel.basicQos(1);
                }catch (Throwable e) {
                    close();
                    this.inited.set(false);
                    return false;
                }
                final Consumer consumer = new DefaultConsumer(channel){
                    /*
                     * 重写回调方法
                     **/
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        T message = null;
                        MessageWorker<T> worker = null;
                        try{
                            for(int i = 0; i < MQConsumer.this.workers.size(); i++){
                                worker = MQConsumer.this.workers.get(i);
                                if(i == 0){
                                    try {
                                        message = mapper.readValue(body, worker.getMessageClass());
                                    }catch (Throwable e){
                                        e.printStackTrace(System.err);
                                    }
                                }
                                try{
                                    worker.doWork(message);
                                    System.out.println("消息处理成功");
                                }catch (Throwable e){
                                    e.printStackTrace(System.err);
                                }
                            }
                        }finally {
                            try {
                                /*
                                 * 手动向服务端发送消息作为应答
                                 * 参数1 当前消息编号
                                 * 参数2 是否已经应答当前消息之前的消息
                                 **/
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            }catch (Throwable e){
                                MQConsumer.this.close();
                                MQConsumer.this.inited.set(false);
                            }
                        }
                    }
                };

                try{
                    /*
                     * 订阅消息并消费
                     * 参数1 队列
                     * 参数2 开启自动应答（默认开启） 此处设置为手动应答 代表消息处理后mq需要收到应答才删除消息
                     * 参数3 设置手动应答回调方法
                     **/
                    System.out.println("消费队列");
                    this.channel.basicConsume(this.queueName, false, consumer);
                } catch (IOException e) {
                    close();
                    this.inited.set(false);
                    return false;
                }
            }
            return false;
        }

        /*
         * @Author gcy
         * @Description 注入加载好的worker
         * @Date 13:20 2020/6/13
         * @Param [worker]
         * @return com.gcy.MQUtils.MQConsumer<T>
         **/
        protected MQConsumer<T> bind(MessageWorker<T> worker){
            if(worker != null){
                this.workers.add(worker);
            }
            return this;
        }

        /*
         * @Author gcy
         * @Description 检查连接/通道是否建立
         * @Date 14:20 2020/6/13
         * @Param []
         * @return boolean
         **/
        public boolean isActive(){
            if(this.connection != null && this.connection.isOpen()){
                if(this.channel != null && this.channel.isOpen()){
                    return true;
                }
            }
            return false;
        }

        public void start(){
            this.watchDog.start();
        }

        /*
         * @Author gcy
         * @Description 监听线程
         * @Date 17:37 2020/6/12
         * @Param
         * @return
         **/
        private Thread watchDog = new Thread(){
            @Override
            public void run() {
                while (!MQConsumer.this.stop){
                    System.out.println(MQConsumer.this.inited+"\n"+ MQConsumer.this.isActive());
                    if(MQConsumer.this.inited.get() && MQConsumer.this.isActive()){
                        try {
                            //休眠10s
                            Thread.sleep(10 * 1000L);
                        } catch (Throwable e) {

                        }
                    }else{
                        if (!MQConsumer.this.stop && !MQConsumer.this.workers.isEmpty()){
                            MQConsumer.this.close();
                            MQConsumer.this.inited.set(false);
                            MQConsumer.this.init();
                        }
                    }
                }
            }
        };

        public void regain(){
            this.stop = false;
            Thread watchDog = new Thread(){
                @Override
                public void run() {
                    while (!MQConsumer.this.stop){
                        if(MQConsumer.this.inited.get() && MQConsumer.this.isActive()){
                            try {
                                Thread.sleep(10 * 1000L);
                            } catch (Throwable e) {

                            }
                        }else{
                            if (!MQConsumer.this.stop && !MQConsumer.this.workers.isEmpty()){
                                MQConsumer.this.close();
                                MQConsumer.this.inited.set(false);
                                MQConsumer.this.init();
                            }
                        }
                    }
                }
            };
            watchDog.start();
        }

    }
}
