package com.runrt.voting.web;

import com.runrt.common.events.VoteRecordedEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/votes")
@RequiredArgsConstructor
public class VoteController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    public ResponseEntity<?> recordVote(@RequestBody VoteRequest req, @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        VoteRecordedEvent event = new VoteRecordedEvent(
                UUID.randomUUID(),
                req.getPollId(),
                req.getOptionId(),
                userId,
                Instant.now()
        );
        kafkaTemplate.send("votes-topic", req.getPollId().toString(), event);
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @Data
    public static class VoteRequest {
        private UUID pollId;
        private UUID optionId;
    }
}
