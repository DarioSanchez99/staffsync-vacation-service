package com.staffsync.vacation.infrastructure.adapter.out.messaging;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.port.out.VacationEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacationEventPublisher implements VacationEventPort {

    private static final String RABBITMQ_EXCHANGE = "staffsync.notifications";
    private static final String ROUTING_KEY_REQUESTED = "vacation.requested";
    private static final String ROUTING_KEY_APPROVED = "vacation.approved";
    private static final String ROUTING_KEY_REJECTED = "vacation.rejected";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishRequested(VacationRequest request) {
        sendToRabbitMQ(ROUTING_KEY_REQUESTED, buildEvent("VACATION_REQUESTED", request));
    }

    @Override
    public void publishApproved(VacationRequest request) {
        sendToRabbitMQ(ROUTING_KEY_APPROVED, buildEvent("VACATION_APPROVED", request));
    }

    @Override
    public void publishRejected(VacationRequest request) {
        sendToRabbitMQ(ROUTING_KEY_REJECTED, buildEvent("VACATION_REJECTED", request));
    }

    private Map<String, Object> buildEvent(String type, VacationRequest request) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", type);
        event.put("vacationId", request.getId().toString());
        event.put("employeeId", request.getEmployeeId().toString());
        event.put("startDate", request.getStartDate().toString());
        event.put("endDate", request.getEndDate().toString());
        event.put("reviewedBy", request.getReviewedBy() != null ? request.getReviewedBy().toString() : null);
        event.put("reviewedByName", request.getReviewedByName());
        event.put("timestamp", Instant.now().toString());
        return event;
    }

    private void sendToRabbitMQ(String routingKey, Map<String, Object> event) {
        try {
            rabbitTemplate.convertAndSend(RABBITMQ_EXCHANGE, routingKey, event);
            log.debug("Published vacation event to RabbitMQ with routing key {}", routingKey);
        } catch (Exception ex) {
            log.error("Failed to publish vacation event to RabbitMQ: {}", ex.getMessage());
        }
    }
}
