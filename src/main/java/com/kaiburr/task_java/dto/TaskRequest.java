package com.kaiburr.task_java.dto;
import jakarta.validation.constraints.NotBlank;

public class TaskRequest {
    @NotBlank(message = "Task ID is required")
    private String id;
    
    @NotBlank(message = "Task name is required")
    private String name;
    
    @NotBlank(message = "Task owner is required")
    private String owner;
    
    @NotBlank(message = "Command is required")
    private String command;

    public TaskRequest() {
    }

    public TaskRequest(String id, String name, String owner, String command) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}