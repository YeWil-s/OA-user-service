package com.oa.approval.vo;

import java.util.ArrayList;
import java.util.List;

public class ApplicationDetailVO extends ApplicationVO {

    private List<String> attachments = new ArrayList<>();
    private List<ApprovalTimelineVO> timeline = new ArrayList<>();

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    public List<ApprovalTimelineVO> getTimeline() { return timeline; }
    public void setTimeline(List<ApprovalTimelineVO> timeline) { this.timeline = timeline; }
}
