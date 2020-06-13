package com.gcy.message;

import com.gcy.queue.Queue;

public class MQBaseModel implements Message{

    private String toJSON(){
        return JSONFormatter.toJSON(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }

    private long mq_sys_create_time = System.currentTimeMillis();//消息创建时间

    private long mq_sys_error_index = 0;//失败计数

    private long mq_sys_sleep_time = 1000;//消息停顿时间

    private Queue mq_sys_err_return_queue;//延迟队列中返回到标准队列

    private String mq_sys_return_json_data;//返回原message_json

    private String mq_sys_return_json_class;//返回原类名

    private int mq_sys_alarm_index = 0;//告警次数

    public long getMq_sys_create_time() {
        return mq_sys_create_time;
    }

    public void setMq_sys_create_time(long mq_sys_create_time) {
        this.mq_sys_create_time = mq_sys_create_time;
    }

    public long getMq_sys_error_index() {
        return mq_sys_error_index;
    }

    public void setMq_sys_error_index(long mq_sys_error_index) {
        this.mq_sys_error_index = mq_sys_error_index;
    }

    public long getMq_sys_sleep_time() {
        return mq_sys_sleep_time;
    }

    public void setMq_sys_sleep_time(long mq_sys_sleep_time) {
        this.mq_sys_sleep_time = mq_sys_sleep_time;
    }

    public Queue getMq_sys_err_return_queue() {
        return mq_sys_err_return_queue;
    }

    public void setMq_sys_err_return_queue(Queue mq_sys_err_return_queue) {
        this.mq_sys_err_return_queue = mq_sys_err_return_queue;
    }

    public String getMq_sys_return_json_data() {
        return mq_sys_return_json_data;
    }

    public void setMq_sys_return_json_data(String mq_sys_return_json_data) {
        this.mq_sys_return_json_data = mq_sys_return_json_data;
    }

    public String getMq_sys_return_json_class() {
        return mq_sys_return_json_class;
    }

    public void setMq_sys_return_json_class(String mq_sys_return_json_class) {
        this.mq_sys_return_json_class = mq_sys_return_json_class;
    }

    public int getMq_sys_alarm_index() {
        return mq_sys_alarm_index;
    }

    public void setMq_sys_alarm_index(int mq_sys_alarm_index) {
        this.mq_sys_alarm_index = mq_sys_alarm_index;
    }
}
