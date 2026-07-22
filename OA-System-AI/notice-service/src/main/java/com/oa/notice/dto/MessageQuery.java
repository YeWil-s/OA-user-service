package com.oa.notice.dto;

import com.oa.model.page.PageQuery;

public class MessageQuery extends PageQuery {

    private Integer msgType;
    private Boolean read;

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
