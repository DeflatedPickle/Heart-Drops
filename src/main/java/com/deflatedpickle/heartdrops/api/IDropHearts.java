package com.deflatedpickle.heartdrops.api;

public interface IDropHearts {
    int getDropAmount();
    int getDropRange();
    boolean doesDropHearts();

    void setDropAmount(int dropAmount);
    void setDropRange(int dropRange);
    void setDropHearts(boolean dropHearts);
}
