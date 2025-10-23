package com.runrt.polls.web;

import com.runrt.common.events.PollCreatedEvent;
import com.runrt.polls.domain.Poll;
import com.runrt.polls.domain.PollOption;
import com.runrt.polls.domain.PollRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PollController {

    private final PollRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreatePollRequest req, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Poll poll = new Poll();
        poll.setTitle(req.getTitle());
        poll.setCreatedBy(userId != null ? userId : "anonymous");
        List<PollOption> options = req.getOptions().stream().map(text -> {
            PollOption opt = new PollOption();
            opt.setText(text);
            opt.setPoll(poll);
            return opt;
        }).collect(Collectors.toList());
        poll.setOptions(options);
        Poll saved = repository.save(poll);

        PollCreatedEvent event = new PollCreatedEvent(
                saved.getId(),
                saved.getTitle(),
                saved.getOptions().stream().map(o -> new PollCreatedEvent.Option(o.getId(), o.getText())).toList()
        );
        kafkaTemplate.send("polls-created-topic", saved.getId().toString(), event);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Poll> list() {
        return repository.findAll();
    }

    @Data
    public static class CreatePollRequest {
        private String title;
        private List<String> options;
    }
}
