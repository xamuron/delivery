package microarch.delivery.core.domain.model.kernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LocationTest {

    @Test
    void shouldBeCorrectWhenParamsAreCorrectOnCreated() {
        // Act
        var result = Location.create(1,1);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(1);
        assertThat(result.getValue().getY()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "11, -1",
            "5, 0",
            "5, 11"
    })
    void shouldReturnErrorWhenParamsAreIncorrectOnCreated(int x, int y) {
        // Act
        var result = Location.create(x, y);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @Test
    void shouldBeEqualsWhenAllParamsAreEqual() {
        // Arrange
        var first = loc(3, 3);
        var second = loc(3, 3);

        // Act
        var result = first.equals(second);

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "4, 3",
            "3, 4"
    })
    void shouldNotBeEqualWhenAnyCoordinateDiffers(int x, int y) {
        // Arrange
        var first = loc(3, 3);
        var second = loc(x, y);

        // Act
        var result = first.equals(second);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideLocationsForDistanceCalculation")
    void shouldReturnCorrectDistanceBetweenTwoLocations(Location from, Location to, int expectedDistance) {
        // Act
        var result = from.calculateDistance(to);

        // Assert
        assertThat(result).isEqualTo(expectedDistance);
    }

    private static Stream<Arguments> provideLocationsForDistanceCalculation() {
        return Stream.of(
                Arguments.of(loc(1, 1), loc(1, 1), 0),
                Arguments.of(loc(1, 3), loc(5, 3), 4),
                Arguments.of(loc(2, 2), loc(2, 8), 6),
                Arguments.of(loc(1, 10), loc(10, 1), 18)
        );
    }

    private static Location loc(int x, int y) {
        return Location.create(x, y).getValue();
    }
}
