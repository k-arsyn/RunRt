package com.runrt.results.service;

import com.runrt.common.events.VoteRecordedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResultsListener {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "votes-topic", groupId = "results-service", containerFactory = "voteEventKafkaListenerContainerFactory")
    public void handleVote(VoteRecordedEvent event) {
        String pollKey = "poll:" + event.getPollId();
        String optionKey = pollKey + ":option:" + event.getOptionId();
        redisTemplate.opsForValue().increment(pollKey + ":total");
        Long optionCount = redisTemplate.opsForValue().increment(optionKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("pollId", event.getPollId());
        payload.put("optionId", event.getOptionId());
        payload.put("optionCount", optionCount);
        messagingTemplate.convertAndSend("/topic/poll-results/" + event.getPollId(), payload);
    }
}
