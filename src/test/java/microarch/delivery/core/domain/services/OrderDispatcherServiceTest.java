package microarch.delivery.core.domain.services;

import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderDispatcherServiceTest {

    @Test
    void shouldAssignOrderToClosestCourier() {
        // Arrange
        var orderDispatcherService = new OrderDispatcherServiceImpl();

        var order = createAnOrder(
                Volume.create(5000).getValue(),
                Location.create(10, 10).getValue()
        );

        var courier1 = Courier.create("Petya", Location.create(1,5).getValue());
        var courier2 = Courier.create("Vasya", Location.create(6,5).getValue());
        List<Courier> couriers = List.of(courier1.getValue(), courier2.getValue());

        // Act
        var result = orderDispatcherService.dispatch(order, couriers);

        //Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isSameAs(courier2.getValue());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        var assignments = courier2.getValue().getAssignments();
        assertThat(assignments.size()).isEqualTo(1);
        assertThat(assignments.get(0).getOrderId()).isEqualTo(order.getId());
        assertThat(assignments.get(0).getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void shouldReturnAnErrorWhenAllCouriersAreBusy() {
        // Arrange
        var orderDispatcherService = new OrderDispatcherServiceImpl();

        var order = createAnOrder(
                Volume.create(20000).getValue(),
                Location.create(10, 10).getValue()
        );

        var newOrder = createAnOrder(
                Volume.create(5000).getValue(),
                Location.create(10, 10).getValue()
        );

        var courier = Courier.create("Petya", Location.create(1,5).getValue());
        var couriers = List.of(courier.getValue());
        var arrangeResult = orderDispatcherService.dispatch(order, couriers);
        assertThat(arrangeResult.isSuccess()).isTrue();

        // Act
        var result = orderDispatcherService.dispatch(newOrder, couriers);

        //Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(OrderDispatcherServiceImpl.Errors.thereAreNoFreeCouriers());
        assertThat(newOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(courier.getValue().getAssignments().size()).isEqualTo(1);
        assertThat(courier.getValue().getAssignments().get(0).getOrderId()).isEqualTo(order.getId());
    }

    @Test
    void shouldReturnAnErrorWhenThereAreNoCouriers() {
        // Arrange
        var orderDispatcherService = new OrderDispatcherServiceImpl();
        var order = createAnOrder(
                Volume.create(20000).getValue(),
                Location.create(10, 10).getValue()
        );
        List<Courier> couriers = List.of();

        // Act
        var result = orderDispatcherService.dispatch(order, couriers);

        //Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(OrderDispatcherServiceImpl.Errors.thereAreNoCouriers());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @ParameterizedTest
    @EnumSource(
            value = OrderStatus.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {"CREATED"}
    )
    void shouldReturnErrorWhenOrderIsNotCreated(OrderStatus orderStatus) {
        // Arrange
        var orderDispatcherService = new OrderDispatcherServiceImpl();
        var order = createAnOrder(
                Volume.create(20000).getValue(),
                Location.create(10, 10).getValue()
        );
        var courier = Courier.create("Petya", Location.create(1,5).getValue());
        var couriers = List.of(courier.getValue());

        assertThat(order.assign().isSuccess()).isTrue();
        if (orderStatus == OrderStatus.COMPLETED) {
            assertThat(order.complete().isSuccess()).isTrue();
        }

        // Act
        var result = orderDispatcherService.dispatch(order, couriers);

        //Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Order.Errors.orderMustBeInCreatedStatusForAssign());
        assertThat(order.getStatus()).isEqualTo(orderStatus);
        assertThat(courier.getValue().getAssignments().isEmpty()).isTrue();
    }

    private Order createAnOrder(Volume volume, Location location) {
        var basketId = UUID.randomUUID();
        return Order.create(basketId, volume, location).getValue();
    }
}
