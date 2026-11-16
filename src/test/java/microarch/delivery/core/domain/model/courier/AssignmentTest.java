package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AssignmentTest {

    @Test
    public void shouldBeCorrectWhenParamsAreCorrectOnCreated() {
        // Arrange
        var location = Location.create(1, 1).getValue();
        var volume = Volume.create(10_000).getValue();
        var orderId = UUID.randomUUID();

        // Act
        var result = Assignment.create(orderId, volume, location);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var assignment = result.getValue();
        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        assertThat(assignment.getLocation().getX()).isEqualTo(1);
        assertThat(assignment.getLocation().getY()).isEqualTo(1);
        assertThat(assignment.getVolume().getValue()).isEqualTo(10_000);
    }

    @ParameterizedTest
    @CsvSource({"5, 5", "4, 5", "6, 5", "5, 4", "5, 6"})
    void shouldCompleteWhenCourierIsWithinOneStep(int x, int y) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var volume = Volume.create(10_000).getValue();
        var assignment = Assignment.create(UUID.randomUUID(), volume, location).getValue();
        var courierLocation = Location.create(x, y).getValue();

        // Act
        var result = assignment.completeAssignment(courierLocation);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @ParameterizedTest
    @CsvSource({"7, 5", "6, 6"})
    void shouldReturnErrorWhenCourierIsTooFar(int x, int y) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var volume = Volume.create(10_000).getValue();
        var assignment = Assignment.create(UUID.randomUUID(), volume, location).getValue();
        var courierLocation = Location.create(x, y).getValue();

        // Act
        var result = assignment.completeAssignment(courierLocation);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("assignment.courier.too.far");
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }
}
