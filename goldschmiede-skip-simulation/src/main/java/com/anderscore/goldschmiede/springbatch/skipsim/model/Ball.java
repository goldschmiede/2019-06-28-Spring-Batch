package com.anderscore.goldschmiede.springbatch.skipsim.model;

import lombok.Data;

@Data
public class Ball {
    public enum Mode {
        NEW, READ, WRITTEN;
    }

    private int no;
    private boolean invalid;
    private Mode mode = Mode.NEW;
}
