package ru.cherniak.produser;

import java.io.Serializable;

public class MyMessage implements Serializable {
    private static final long serialVersionUID = -1650136059587331366L;

    private String msg;

    public String getMsg() {
        return msg;
    }

    public MyMessage(String msg) {
        this.msg = msg;
    }
}
