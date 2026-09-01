package com.studymanager.dto.request;

import java.time.ZonedDateTime;

public class ResolveSessionRequest {

    private String action;           // SAVE_AT_HEARTBEAT | CONTINUE | MANUAL
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Long duration;
    private String metadata;

    public ResolveSessionRequest() {}

    public String getAction() { return action; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public Long getDuration() { return duration; }
    public String getMetadata() { return metadata; }

    public void setAction(String action) { this.action = action; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(ZonedDateTime endTime) { this.endTime = endTime; }
    public void setDuration(Long duration) { this.duration = duration; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
