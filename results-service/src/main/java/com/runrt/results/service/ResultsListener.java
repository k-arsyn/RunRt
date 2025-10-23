package com.runrt.results.service;

import com.runrt.common.events.VoteRecordedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultsListener {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "votes-topic", groupId = "results-service", containerFactory = "voteEventKafkaListenerContainerFactory")
    public void handleVote(VoteRecordedEvent event) {
        try {
            log.info("Kafka vote received: pollId={}, optionId={}", event.getPollId(), event.getOptionId());

            String pollKey = "poll:" + event.getPollId();
            String optionKey = pollKey + ":option:" + event.getOptionId();
            redisTemplate.opsForValue().increment(pollKey + ":total");
            Long optionCount = redisTemplate.opsForValue().increment(optionKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("pollId", event.getPollId());
            payload.put("optionId", event.getOptionId());
            payload.put("optionCount", optionCount == null ? 0L : optionCount);

            String destination = "/topic/poll-results/" + event.getPollId();
            messagingTemplate.convertAndSend(destination, payload);
            log.info("WS sent to {} => {}", destination, payload);
        } catch (Exception ex) {
            log.error("Failed to process vote event", ex);
        }
    }
}
