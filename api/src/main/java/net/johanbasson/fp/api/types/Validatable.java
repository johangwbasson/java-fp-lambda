package net.johanbasson.fp.api.types;

import com.jnape.palatable.lambda.adt.Maybe;
import net.johanbasson.fp.api.system.errors.FieldError;

public interface Validatable {

    Maybe<FieldError> validate();

}
