package com.taskmaple.todo.web;

import com.taskmaple.todo.infrastructure.support.SeedService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only endpoint to reset the in-memory store to the canonical Background, giving e2e
 * scenarios a deterministic starting point. Disabled unless {@code todo.test-support=true}.
 */
@RestController
@RequestMapping("/api/v1/test")
@ConditionalOnProperty(name = "todo.test-support", havingValue = "true")
public class TestSupportController {

    private final SeedService seed;

    public TestSupportController(SeedService seed) {
        this.seed = seed;
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        seed.resetToBackground();
        return ResponseEntity.noContent().build();
    }
}
