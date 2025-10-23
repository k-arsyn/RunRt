package com.runrt.polls.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "poll_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollOption {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    @JsonBackReference
    private Poll poll;

    @Column(nullable = false)
    private String text;
}
