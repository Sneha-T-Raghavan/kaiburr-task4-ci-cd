package com.kaiburr.task_java.controller;
import com.kaiburr.task_java.model.Task;
import com.kaiburr.task_java.dto.TaskRequest;
import com.kaiburr.task_java.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;  

import java.util.List;



@RestController 
@RequestMapping("/tasks") 
@CrossOrigin(origins = "http://localhost:3000")
  public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String id) {
        if (id != null && !id.isEmpty()) {
            Task task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        }
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        Task task = taskService.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task with id " + id + " deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Task>> findTasksByName(@RequestParam String name) {
        List<Task> tasks = taskService.findTasksByName(name);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<Task> executeTask(@PathVariable String id) {
        Task task = taskService.executeTask(id);
        return ResponseEntity.ok(task);
    }
}