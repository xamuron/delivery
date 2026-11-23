package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Volume extends ValueObject<Volume> {
    // Объем в миллилитрах
    private final int value;

    public static Result<Volume, Error> create(int value) {
        if (value <= 0) {
            return Result.failure(GeneralErrors.valueMustBeGreaterThan("value", value, 0));
        }

        return Result.success(new Volume(value));
    }

    public Result<Volume, Error> add(Volume other) {
        if (other == null) {
            return Result.failure(GeneralErrors.valueIsRequired("other volume"));
        }

        long sum = (long) this.value + other.value;
        if (sum > Integer.MAX_VALUE) {
            return Result.failure(GeneralErrors.valueMustBeLessOrEqual("volume", sum, (long) Integer.MAX_VALUE));
        }

        return Result.success(new Volume((int) sum));
    }

    public boolean isGreaterThan(Volume other) {
        Objects.requireNonNull(other, "other must not be null");
        return this.value > other.value;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.value);
    }
}
