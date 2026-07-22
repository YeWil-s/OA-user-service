package com.oa.notice.dto;

public class UnreadCountResponse {

    private long noticeUnread;
    private long messageUnread;
    private long totalUnread;

    public UnreadCountResponse() {
    }

    public UnreadCountResponse(long noticeUnread, long messageUnread) {
        this.noticeUnread = noticeUnread;
        this.messageUnread = messageUnread;
        this.totalUnread = noticeUnread + messageUnread;
    }

    public long getNoticeUnread() {
        return noticeUnread;
    }

    public void setNoticeUnread(long noticeUnread) {
        this.noticeUnread = noticeUnread;
    }

    public long getMessageUnread() {
        return messageUnread;
    }

    public void setMessageUnread(long messageUnread) {
        this.messageUnread = messageUnread;
    }

    public long getTotalUnread() {
        return totalUnread;
    }

    public void setTotalUnread(long totalUnread) {
        this.totalUnread = totalUnread;
    }
}
