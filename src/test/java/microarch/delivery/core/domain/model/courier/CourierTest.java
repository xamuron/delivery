package microarch.delivery.core.domain.model.courier;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CourierTest {

    @Test
    void shouldBeCorrectWhenParamsAreCorrectOnCreated() {
        // Arrange
        var location = Location.create(5, 5).getValue();

        // Act
        var result = Courier.create("Ivan", location);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var courier = result.getValue();
        assertThat(courier.getId()).isNotNull();
        assertThat(courier.getName()).isEqualTo("Ivan");
        assertThat(courier.getLocation()).isEqualTo(location);
        assertThat(courier.getMaxVolume().getValue()).isEqualTo(20_000);
        assertThat(courier.getAssignments().isEmpty()).isTrue();
    }

    @Test
    void shouldTakeOrderWhenTotalVolumeEqualsMaximum() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Ivan", location).getValue();
        var volume = Volume.create(10_000).getValue();
        var firstOrder = Order.create(UUID.randomUUID(), volume, location).getValue();
        var secondOrder = Order.create(UUID.randomUUID(), volume, location).getValue();
        assertThat(courier.takeOrder(firstOrder).isSuccess()).isTrue();

        // Act
        var availability = courier.canTakeOrder(secondOrder);
        var result = courier.takeOrder(secondOrder);

        // Assert
        assertThat(availability.isSuccess()).isTrue();
        assertThat(availability.getValue()).isTrue();
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments().size()).isEqualTo(2);
        var assignment = courier.getAssignments().get(1);
        assertThat(assignment.getOrderId()).isEqualTo(secondOrder.getId());
        assertThat(assignment.getVolume()).isEqualTo(secondOrder.getVolume());
        assertThat(assignment.getLocation()).isEqualTo(secondOrder.getLocation());
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @ParameterizedTest
    @ValueSource(ints = {10_001, Integer.MAX_VALUE})
    void shouldReturnErrorWhenTotalVolumeExceedsMaximum(int newOrderVolume) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Ivan", location).getValue();
        var firstVolume = Volume.create(10_000).getValue();
        var firstOrder = Order.create(UUID.randomUUID(), firstVolume, location).getValue();
        var secondVolume = Volume.create(newOrderVolume).getValue();
        var secondOrder = Order.create(UUID.randomUUID(), secondVolume, location).getValue();
        assertThat(courier.takeOrder(firstOrder).isSuccess()).isTrue();

        // Act
        var availability = courier.canTakeOrder(secondOrder);
        var result = courier.takeOrder(secondOrder);

        // Assert
        assertThat(availability.isSuccess()).isTrue();
        assertThat(availability.getValue()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.totalVolumeShouldNotExceedMax());
        assertThat(courier.getAssignments().size()).isEqualTo(1);
        assertThat(courier.getAssignments().get(0).getOrderId()).isEqualTo(firstOrder.getId());
    }

    @Test
    void shouldFreeCapacityWhenAssignmentIsCompleted() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Ivan", location).getValue();
        var volume = Volume.create(20_000).getValue();
        var firstOrder = Order.create(UUID.randomUUID(), volume, location).getValue();
        var secondOrder = Order.create(UUID.randomUUID(), volume, location).getValue();
        assertThat(courier.takeOrder(firstOrder).isSuccess()).isTrue();
        var assignment = courier.getAssignments().get(0);

        // Act
        var completion = courier.completeAssignment(assignment.getId());
        var result = courier.takeOrder(secondOrder);

        // Assert
        assertThat(completion.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments().size()).isEqualTo(2);
        assertThat(courier.getAssignments().get(1).getOrderId()).isEqualTo(secondOrder.getId());
    }

    @Test
    void shouldReturnErrorWhenAssignmentBelongsToAnotherCourier() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Ivan", location).getValue();
        var anotherCourier = Courier.create("Petr", location).getValue();
        var volume = Volume.create(5_000).getValue();
        var order = Order.create(UUID.randomUUID(), volume, location).getValue();
        assertThat(anotherCourier.takeOrder(order).isSuccess()).isTrue();
        var assignment = anotherCourier.getAssignments().get(0);

        // Act
        var result = courier.completeAssignment(assignment.getId());

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.notFound("assignment", assignment.getId()));
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void shouldMoveWhenDestinationIsOneStepAway() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Ivan", location).getValue();
        var destination = Location.create(6, 5).getValue();

        // Act
        var result = courier.moveTo(destination);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(destination);
    }

    @ParameterizedTest
    @CsvSource({"7, 5", "6, 6"})
    void shouldReturnErrorWhenDestinationIsMoreThanOneStepAway(int x, int y) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Ivan", location).getValue();
        var destination = Location.create(x, y).getValue();

        // Act
        var result = courier.moveTo(destination);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.courierStepCanNotBeMoreThanOnePoint());
        assertThat(courier.getLocation()).isEqualTo(location);
    }
}
