package com.oa.notice.dto;

import com.oa.common.page.PageQuery;

public class NoticeQuery extends PageQuery {

    private String keyword;
    private Integer noticeType;
    private Boolean unreadOnly = false;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(Integer noticeType) {
        this.noticeType = noticeType;
    }

    public Boolean getUnreadOnly() {
        return unreadOnly;
    }

    public void setUnreadOnly(Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }
}
