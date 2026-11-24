package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderTest {

    @Test
    void shouldBeCorrectWhenParamsAreCorrectOnCreated() {
        // Arrange
        var basketId = UUID.randomUUID();
        var volume = Volume.create(5_000).getValue();
        var location = Location.create(5, 5).getValue();

        // Act
        var result = Order.create(basketId, volume, location);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var order = result.getValue();
        assertThat(order.getId()).isEqualTo(basketId);
        assertThat(order.getVolume()).isEqualTo(volume);
        assertThat(order.getLocation()).isEqualTo(location);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldChangeStatusToAssignedWhenCurrentStatusIsCreated() {
        // Arrange
        var volume = Volume.create(5_000).getValue();
        var location = Location.create(5, 5).getValue();
        var order = Order.create(UUID.randomUUID(), volume, location).getValue();

        // Act
        var result = order.assign();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void shouldCompleteWhenOrderIsAssigned() {
        // Arrange
        var volume = Volume.create(5_000).getValue();
        var location = Location.create(5, 5).getValue();
        var order = Order.create(UUID.randomUUID(), volume, location).getValue();
        assertThat(order.assign().isSuccess()).isTrue();

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldReturnErrorWhenCompletingCreatedOrder() {
        // Arrange
        var volume = Volume.create(5_000).getValue();
        var location = Location.create(5, 5).getValue();
        var order = Order.create(UUID.randomUUID(), volume, location).getValue();

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Order.Errors.orderMustBeInAssignedStatusForComplete());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
