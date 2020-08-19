package io.depa.common.data.audit;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Date audit object.
 *
 * @author Artem Kostritsa
 */
@Accessors(chain = true)
@Data
public abstract class DateAudit {

    private String createdAt;
    private String updatedAt;

    protected static <T extends DateAudit> void setTimestamp(T object) {
        String timestamp = LocalDateTime.now().toString();
        object.setCreatedAt(timestamp);
        object.setUpdatedAt(timestamp);
    }

    protected static <T extends DateAudit> void setUpdateTimestamp(T object) {
        String timestamp = LocalDateTime.now().toString();
        object.setUpdatedAt(timestamp);
    }
}