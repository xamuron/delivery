package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Courier extends Aggregate<UUID> {
    private String name;
    private Location location;
    private Volume maxVolume;
    private final List<Assignment> assignments = new ArrayList<>();

    private Courier() {
    }

    private Courier(String name, Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.location = location;
        this.maxVolume = Volume.create(20000).getValue();
    }

    public List<Assignment> getAssignments() {
        return List.copyOf(assignments);
    }

    public static Result<Courier, Error> create(String name, Location location) {
        var error = Guard.againstNullOrEmpty(name, "name");
        if (error != null) {
            return Result.failure(error);
        }
        if (location == null) return Result.failure(GeneralErrors.valueIsRequired("location"));

        return Result.success(new Courier(name, location));
    }

    public Result<Boolean, Error> canTakeOrder(Order order) {
        if (order == null) return Result.failure(GeneralErrors.valueIsRequired("order"));

        var totalVolume = order.getVolume();
        for (Assignment assignment : assignments) {
            if (assignment.getStatus() == AssignmentStatus.ASSIGNED) {
                var sum = totalVolume.add(assignment.getVolume());
                if (sum.isFailure()) return Result.success(false);
                totalVolume = sum.getValue();
            }
        }
        return Result.success(!totalVolume.isGreaterThan(maxVolume));
    }

    public UnitResult<Error> takeOrder(Order order) {
        if (order == null) return UnitResult.failure(GeneralErrors.valueIsRequired("order"));
        var availability = canTakeOrder(order);
        if (availability.isFailure()) return UnitResult.failure(availability.getError());
        if (!availability.getValue()) return UnitResult.failure(Errors.totalVolumeShouldNotExceedMax());
        var newAssignment = Assignment.create(order.getId(), order.getVolume(), order.getLocation());
        assignments.add(newAssignment.getValue());
        return UnitResult.success();
    }

    public UnitResult<Error> completeAssignment(UUID assignmentId) {
        var error = Guard.againstNullOrEmpty(assignmentId, "assignmentId");
        if (error != null) {
            return UnitResult.failure(error);
        }
        for (Assignment assignment : assignments) {
            if (assignment.getId().equals(assignmentId)) {
                return assignment.completeAssignment(this.location);
            }
        }

        return UnitResult.failure(GeneralErrors.notFound("assignment", assignmentId));
    }

    public UnitResult<Error> moveTo(Location location) {
        if (location == null) return UnitResult.failure(GeneralErrors.valueIsRequired("location"));
        // судя по интерфейсу по диагонали ходить нельзя, но пока пусть так
        if (this.location.calculateDistance(location) > 1) return UnitResult.failure(Errors.courierStepCanNotBeMoreThanOnePoint());

        this.location = location;
        return UnitResult.success();
    }

    public static class Errors {
        public static Error totalVolumeShouldNotExceedMax() {
            return Error.of("total.volume.should.not.exceed.max", "Общий вес заказов у Курьера не может превышать максимальный");
        }

        public static Error courierStepCanNotBeMoreThanOnePoint() {
            return Error.of("courier.step.can.not.be.more.than.one.point", "Курьер не может переместиться более чем на 1 шаг");
        }
    }
}
