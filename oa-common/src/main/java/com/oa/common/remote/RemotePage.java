package com.oa.common.remote;

import java.util.List;

public class RemotePage<T> {
    private List<T> records;

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
