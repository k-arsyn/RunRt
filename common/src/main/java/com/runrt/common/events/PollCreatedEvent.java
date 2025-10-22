package com.runrt.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollCreatedEvent {
    private UUID pollId;
    private String title;
    private List<Option> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private UUID optionId;
        private String text;
    }
}
