package com.gcy;

import com.gcy.entity.TestMessageProducer;
import com.gcy.queue.Queue;


/*
 * @Author gcy
 * @Description 生产者测试类
 * @Date 16:30 2020/6/12
 * @Param
 * @return
 **/
public class TestPublish {
    public static void main(String[] args) {
        TestMessageProducer testMessage = new TestMessageProducer("level 10");
        //无优先级
        MQUtils.publish(Queue.TEST_QUEUE_1, testMessage);
        //优先级
        MQUtils.publish(Queue.TEST_QUEUE_LEVEL, testMessage,QueueLevel.HIGHEST.getLevel(),10);

    }
}
