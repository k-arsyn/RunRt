package com.runrt.voting.service;

import com.runrt.common.events.VoteRecordedEvent;
import com.runrt.voting.domain.Vote;
import com.runrt.voting.domain.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteConsumer {
    private final VoteRepository repository;

    @KafkaListener(topics = "votes-topic", groupId = "voting-service")
    public void handle(VoteRecordedEvent event) {
        Vote v = Vote.builder()
                .pollId(event.getPollId())
                .optionId(event.getOptionId())
                .userId(event.getUserId())
                .createdAt(event.getTimestamp())
                .build();
        repository.save(v);
    }
}
