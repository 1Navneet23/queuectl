package org.navneet.queuectl;
import java.time.Instant;
import java.util.UUID;

public class Job {

    private String id;
    private String command;
    private JobState state;
    private int attempts;
    private int maxRetries;
    private Instant createdAt;
    private Instant updatedAt;
    public Job(String command, int maxRetries) {
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.state = JobState.PENDING;
        this.attempts = 0;
        this.maxRetries = maxRetries;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public Job(String id,String command,JobState state,int attempts,int maxRetries,Instant createdAt,Instant updatedAt){

        this.id=(id==null || id.isBlank())?UUID.randomUUID().toString():id;
        this.command=command;
        this.state=state;
        this.attempts=attempts;
        this.maxRetries=maxRetries;
        this.createdAt=createdAt;
        this.updatedAt=updatedAt;
    }
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id=id;
    }
    public String getCommand(){
        return command;
    }
    public void setCommand(String command){
        this.command=command;
    }
    public JobState getState(){
        return state;
    }
    public void setState(JobState state){
        this.state=state;
    }
    public int getAttempts(){
        return attempts;
    }
    public void setAttempts(int attempts){
        this.attempts=attempts;
    }
    public int getMaxRetries(){
        return maxRetries;
    }
    public void setMaxRetries(int maxRetries){
        this.maxRetries=maxRetries;
    }
    public Instant getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt){
        this.createdAt=createdAt;
    }
    public Instant getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt){
        this.updatedAt=updatedAt;
    }








}
