package com.example.myprayertimes;

public class ValContainer<T> {
    private T val;
    public ValContainer() {}
    public ValContainer(T v) {
        this.val = v;
    }
    public T getVal() {
        return val;
    }
    public void setVal(T val) {
        this.val = val;
    }
}
