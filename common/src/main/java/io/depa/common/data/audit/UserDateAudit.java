package io.depa.common.data.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Date & User audit object.
 *
 * @author Artem Kostritsa
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class UserDateAudit extends DateAudit {

    private Long createdBy;
    private Long updatedBy;
}
