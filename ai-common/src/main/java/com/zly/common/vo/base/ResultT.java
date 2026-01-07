package com.zly.common.vo.base;

import lombok.Data;

@Data
public class ResultT<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResultT<T> success(T data) {
        ResultT<T> r = new ResultT<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> ResultT<T> error(String message) {
        ResultT<T> r = new ResultT<>();
        r.setCode(400);
        r.setMessage(message);
        r.setData(null);
        return r;
    }
}

