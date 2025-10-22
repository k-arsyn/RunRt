package com.runrt.voting.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID pollId;

    @Column(nullable = false)
    private UUID optionId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant createdAt;
}



