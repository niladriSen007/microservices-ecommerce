package com.niladri.common.dtos.events;

import com.niladri.common.dtos.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {
    private String eventId;
    private EventType eventType;
    private String occurredAt;
}
