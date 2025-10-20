package com.kaiburr.task_java.service;

import com.kaiburr.task_java.dto.TaskRequest;
import com.kaiburr.task_java.exception.TaskNotFoundException;
import com.kaiburr.task_java.model.Task;
import com.kaiburr.task_java.model.TaskExecution;
import com.kaiburr.task_java.repo.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile(".*rm\\s+-rf.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*>\\s*/dev/.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*mkfs.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*dd\\s+if=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*fork\\s*bomb.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*:\\(\\).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*shutdown.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*reboot.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*wget.*\\|\\s*sh.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*curl.*\\|\\s*sh.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*chmod.*777.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*>\\s*/etc/.*", Pattern.CASE_INSENSITIVE)
    };

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public Task createTask(TaskRequest request) {
        validateCommand(request.getCommand());
        
        Task task = new Task();
        task.setId(request.getId());
        task.setName(request.getName());
        task.setOwner(request.getOwner());
        task.setCommand(request.getCommand());
        
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    public List<Task> findTasksByName(String name) {
        List<Task> tasks = taskRepository.findByNameContaining(name);
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("No tasks found with name containing: " + name);
        }
        return tasks;
    }

    public Task executeTask(String id) {
        Task task = getTaskById(id);
        
        TaskExecution execution = new TaskExecution();
        execution.setStartTime(new Date());
        
        try {
            String output = executeCommand(task.getCommand());
            execution.setOutput(output);
        } catch (Exception e) {
            execution.setOutput("Error: " + e.getMessage());
        }
        
        execution.setEndTime(new Date());
        task.addTaskExecution(execution);
        
        return taskRepository.save(task);
    }

    private void validateCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be empty");
        }

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(command).matches()) {
                throw new IllegalArgumentException("Command contains potentially dangerous operations and is not allowed");
            }
        }

        if (command.contains("&&") || command.contains("||") || command.contains(";")) {
            throw new IllegalArgumentException("Chained commands are not allowed for security reasons");
        }

        if (command.matches(".*[<>].*") && !command.matches(".*echo.*")) {
            throw new IllegalArgumentException("File redirection is not allowed");
        }
    }

    private String executeCommand(String command) throws Exception {
        StringBuilder output = new StringBuilder();
        
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
        
        if (os.contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
        }
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroy();
            throw new Exception("Command execution timed out after 30 seconds");
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            output.append("\nExit code: ").append(exitCode);
        }
        
        return output.toString().trim();
    }
}