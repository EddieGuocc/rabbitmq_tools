package com.gcy;

/*
 * @Author gcy
 * @Description 队列优先级
 * @Date 14:34 2020/6/6
 * @Param
 * @return
 **/
public enum QueueLevel {
    LOWEST(0),
    LOW(3),
    MIDDLE(5),
    HIGH(7),
    HIGHEST(10);

    private int level;

    private QueueLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }




}
