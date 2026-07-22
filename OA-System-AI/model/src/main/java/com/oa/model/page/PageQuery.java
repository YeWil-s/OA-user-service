package com.oa.model.page;

public class PageQuery {

    private long current = 1;
    private long size = 10;

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = Math.max(1, current);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size <= 0 ? 10 : Math.min(size, 200);
    }
}
