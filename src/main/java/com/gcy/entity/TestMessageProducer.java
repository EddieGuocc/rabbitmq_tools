package com.gcy.entity;

import com.gcy.message.MQBaseModel;
import com.gcy.message.Message;

public class TestMessageProducer extends MQBaseModel {
    private String msg_content;

    public String getMsg_content() {
        return msg_content;
    }

    public void setMsg_content(String msg_content) {
        this.msg_content = msg_content;
    }

    public TestMessageProducer(String msg_content) {
        this.msg_content = msg_content;
    }
}
