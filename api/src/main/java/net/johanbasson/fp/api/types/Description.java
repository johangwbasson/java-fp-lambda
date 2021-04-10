package net.johanbasson.fp.api.types;

import com.jnape.palatable.lambda.adt.Maybe;
import net.johanbasson.fp.api.system.errors.FieldError;

public class Description implements Validatable{
    private final String description;

    private Description(String description) {
        this.description = description;
    }

    public static Description of(String value) {
        return new Description(value);
    }

    public String asString() {
        return description;
    }

    @Override
    public Maybe<FieldError> validate() {
        if (description != null && description.length() > 512) {
            return Maybe.just(FieldError.of("description", "Description cannot be longer than 255 characters"));
        }
        return Maybe.nothing();
    }
}
