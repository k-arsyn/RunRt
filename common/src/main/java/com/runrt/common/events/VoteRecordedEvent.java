package com.runrt.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRecordedEvent {
    private UUID voteId;
    private UUID pollId;
    private UUID optionId;
    private UUID userId;
    private Instant timestamp;
}
