package net.johanbasson.fp.api.types;

import com.jnape.palatable.lambda.adt.Maybe;
import net.johanbasson.fp.api.system.errors.FieldError;
import org.apache.commons.lang3.StringUtils;

public final class Name implements Validatable {
    private final String value;

    private Name(String value) {
        this.value = value;
    }

    public static Name of(String value) {
        return new Name(value);
    }

    public String asString() {
        return value;
    }

    public Maybe<FieldError> validate() {
        if (StringUtils.isEmpty(value)) {
            return Maybe.just(FieldError.of("name", "Name cannot be empty"));
        }

        if (value.length() < 3) {
            return Maybe.just(FieldError.of("name", "Name cannot be smaller than three characters"));
        }

        if (value.length() > 255) {
            return Maybe.just(FieldError.of("name", "NAme cannot be larger than 255 characters"));
        }

        return Maybe.nothing();
    }
}
