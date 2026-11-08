package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends ValueObject<Location> {
    private static final int MIN = 1;
    private static final int MAX = 10;
    private int x;
    private int y;

    private Location() {
    }

    public static Result<Location, Error> create(int x, int y) {
        if ((x < MIN) || (x > MAX)) {
            return Result.failure(GeneralErrors.valueIsOutOfRange("x", x, MIN, MAX));
        }
        if ((y < MIN) || (y > MAX)) {
            return Result.failure(GeneralErrors.valueIsOutOfRange("y", y, MIN, MAX));
        }
        return Result.success(new Location(x, y));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.x, this.y);
    }

    public int calculateDistance(Location to) {
        return Math.abs(this.x - to.x) + Math.abs(this.y - to.y);
    }
}
