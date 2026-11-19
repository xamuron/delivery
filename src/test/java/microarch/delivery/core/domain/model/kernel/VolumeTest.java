package microarch.delivery.core.domain.model.kernel;

import libs.errs.GeneralErrors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class VolumeTest {

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
