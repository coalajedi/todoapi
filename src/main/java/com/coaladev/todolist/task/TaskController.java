package com.coaladev.todolist.task;

import com.coaladev.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var userId = request.getAttribute("userId");
        taskModel.setUserId((UUID) userId);

        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task start | end date must be greater than current date.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task end date must be greater than start date.");
        }

        var createdTask = this.taskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var userId = request.getAttribute("userId");

        return this.taskRepository.findByUserId((UUID) userId);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @NotNull HttpServletRequest request, @PathVariable UUID taskId) {
        var userId = request.getAttribute("userId");

        var task = this.taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found.");
        }

        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This task does not belong to you!");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var updatedTask =  this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }
}
