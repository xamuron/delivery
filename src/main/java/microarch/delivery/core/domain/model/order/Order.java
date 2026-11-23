package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Getter
public class Order extends Aggregate<UUID> {
    private Location location;
    private Volume volume;
    private OrderStatus status;

    private Order() {
    }

    private Order(UUID basketId, Volume volume, Location location) {
        super(basketId);
        this.location = location;
        this.volume = volume;
        this.status = OrderStatus.CREATED;
    }

    public static Result<Order, Error> create(UUID basketId, Volume volume, Location location) {
        var error = Guard.againstNullOrEmpty(basketId, "orderId");
        if (error != null) {
            return Result.failure(error);
        }
        if (volume == null) {
            return Result.failure(GeneralErrors.valueIsRequired("volume"));
        }
        if (location == null) {
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        }
        return Result.success(new Order(basketId, volume, location));
    }

    public UnitResult<Error> assign() {
        if (this.status != OrderStatus.CREATED) return UnitResult.failure(Errors.orderMustBeInCreatedStatusForAssign());
        this.status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (this.status != OrderStatus.ASSIGNED) return UnitResult.failure(Errors.orderMustBeInAssignedStatusForComplete());
        this.status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    public static class Errors {
        public static Error orderMustBeInCreatedStatusForAssign() {
            return Error.of("order.must.be.in.created.status", "Назначить можно только заказ в статусе Создан");
        }

        public static Error orderMustBeInAssignedStatusForComplete() {
            return Error.of("order.must.be.in.assigned.status", "Завершить можно только заказ в статусе Назначен");
        }
    }
}
