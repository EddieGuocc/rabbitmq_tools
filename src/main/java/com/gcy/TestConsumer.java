package com.gcy;

import com.gcy.entity.TestMessageConsumer;
import com.gcy.message.MessageWorker;
import com.gcy.queue.Queue;


/*
 * @Author gcy
 * @Description 消费者测试类
 * @Date 16:30 2020/6/12
 * @Param
 * @return
 **/
public class TestConsumer implements MessageWorker<TestMessageConsumer> {
    public static void main(String[] args) {
        //绑定无优先级队列
        MQUtils.bind(Queue.TEST_QUEUE_1, new TestConsumer()).start();
        //绑定优先级队列
        //MQUtils.bind(Queue.TEST_Queue_3, new TestConsumer()).start();
        System.out.println("TEST_QUEUE_3 Listener Started...");
    }

    @Override
    public void doWork(TestMessageConsumer message) {
        //处理业务逻辑
        System.out.println("获取到消息"+message.toString());
    }

    @Override
    public Class<TestMessageConsumer> getMessageClass() {
        return TestMessageConsumer.class;
    }
}
