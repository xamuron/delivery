package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class OrderDispatcherServiceImpl implements OrderDispatcherService {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(couriers, "couriers");
        if (order.getStatus() != OrderStatus.CREATED)  return Result.failure(Order.Errors.orderMustBeInCreatedStatusForAssign());
        if (couriers.isEmpty()) return Result.failure(Errors.thereIsNoCouriers());
        var availableCouriers = couriers.stream().filter(c -> c.canTakeOrder(order).getValue()).toList();
        if (availableCouriers.isEmpty()) return Result.failure(Errors.thereIsNoFreeCouriers());

        Courier winner = null;
        int minDistance = Integer.MAX_VALUE;
        for (Courier courier : availableCouriers) {
            int distance = courier.getLocation().calculateDistance(order.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                winner = courier;
            }
        }

        var takeOrderResult = winner.takeOrder(order);
        if (takeOrderResult.isFailure()) return Result.failure(takeOrderResult.getError());
        var assignResult = order.assign();
        if (assignResult.isFailure()) return Result.failure(assignResult.getError());

        return Result.success(winner);
    }

    public static class Errors {
        public static Error thereIsNoFreeCouriers() {
            return Error.of("there.are.no.free.couriers", "Для назначения заказа нет свободных курьеров: все курьеры переполнены");
        }

        public static Error thereIsNoCouriers() {
            return Error.of("there.are.no.couriers", "Для назначения заказа нет курьеров");
        }
    }
}
