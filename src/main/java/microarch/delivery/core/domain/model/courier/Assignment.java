package microarch.delivery.core.domain.model.courier;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Getter
public class Assignment extends BaseEntity<UUID> {
    private UUID orderId;
    private Volume volume;
    private Location location;
    private AssignmentStatus status;

    private Assignment() {
    }

    private Assignment(UUID orderId, Volume volume, Location location) {
        super(UUID.randomUUID());
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = AssignmentStatus.ASSIGNED;
    }

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {
        // проверка уникальности orderId по идее как понимаю будет возложена на самого Courier в будущем
        var error = Guard.againstNullOrEmpty(orderId, "orderId");
        if (error != null) {
            return Result.failure(error);
        }

        if (volume == null) {
            return Result.failure(GeneralErrors.valueIsRequired("volume"));
        }

        if (location == null) {
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        }

        return Result.success(new Assignment(orderId, volume, location));
    }

    public UnitResult<Error> completeAssignment(Location courierLocation) {
        if (courierLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("courierLocation"));
        }
        if (this.status == AssignmentStatus.COMPLETED) {
            return UnitResult.failure(Error.of("assignment.already.completed", "Назначение уже завершено"));
        }
        if (courierLocation.calculateDistance(this.location) > 1) {
            return UnitResult.failure(Error.of("assignment.courier.too.far", "Для заврешения курьер должен находиться не далее одного шага от доставки"));
        }
        this.status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }
}
