package com.gcy.entity;

import com.gcy.message.MQBaseModel;

public class TestMessageConsumer extends MQBaseModel {
    private String msg_content;

    public String getMsg_content() {
        return msg_content;
    }

    public void setMsg_content(String msg_content) {
        this.msg_content = msg_content;
    }
}
