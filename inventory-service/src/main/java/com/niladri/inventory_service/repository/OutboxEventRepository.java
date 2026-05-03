package com.niladri.inventory_service.repository;

import com.niladri.inventory_service.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
                    SELECT * FROM outbox_event
                    WHERE status = 'NEW' AND next_attempt_at <= now()
                    ORDER BY created_at
                    LIMIT 50
                    FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> fetchBatchForUpdate();
}
