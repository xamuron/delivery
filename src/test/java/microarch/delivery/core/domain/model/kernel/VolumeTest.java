package microarch.delivery.core.domain.model.kernel;

import libs.errs.GeneralErrors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class VolumeTest {

    @Test
    void shouldAddVolumesWithoutChangingOriginalValues() {
        // Arrange
        var first = Volume.create(5_000).getValue();
        var second = Volume.create(10_000).getValue();

        // Act
        var result = first.add(second);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(15_000);
        assertThat(first.getValue()).isEqualTo(5_000);
        assertThat(second.getValue()).isEqualTo(10_000);
    }

    @Test
    void shouldReturnErrorWhenAddingNullVolume() {
        // Arrange
        var volume = Volume.create(5_000).getValue();

        // Act
        var result = volume.add(null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("other"));
    }

    @ParameterizedTest
    @CsvSource({"5000, 10000, false", "10000, 10000, false", "10001, 10000, true"})
    void shouldCompareVolumes(int firstValue, int secondValue, boolean expected) {
        // Arrange
        var first = Volume.create(firstValue).getValue();
        var second = Volume.create(secondValue).getValue();

        // Act
        var result = first.isGreaterThan(second);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5500, 30000, Integer.MAX_VALUE})
    void shouldBeCorrectWhenValueIsCorrectOnCreated(int value) {
        // Act
        var result = Volume.create(value);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void shouldReturnErrorWhenValueIsIncorrectOnCreated(int value) {
        // Act
        var result = Volume.create(value);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueMustBeGreaterThan("value", value, 0));
    }

    @Test
    void shouldBeEqualsWhenValuesAreEqual() {
        // Arrange
        var first = Volume.create(5500).getValue();
        var second = Volume.create(5500).getValue();

        // Act
        var result = first.equals(second);

        // Assert
        assertThat(result).isTrue();
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldNotBeEqualsWhenValuesAreDifferent() {
        // Arrange
        var first = Volume.create(5500).getValue();
        var second = Volume.create(5501).getValue();

        // Act
        var result = first.equals(second);

        // Assert
        assertThat(result).isFalse();
        assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
    }
}
