package net.johanbasson.fp.api.system.errors;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import net.johanbasson.fp.api.types.Name;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class ValidationUtilTest {

    @Test
    public void testNoError() {
        ValidationUtil.validate(Collections.singletonList(Tuple2.tuple("name", Name.of("John"))))
                .match(
                        unit -> {
                            // Expected
                            return Unit.UNIT;
                        },
                        validationErrors -> {
                            fail("There should be no errors but received errors");
                            return Unit.UNIT;
                        });
    }

    @Test
    public void testError() {
        ValidationUtil.validate(Collections.singletonList(Tuple2.tuple("name", Name.of("de"))))
                .match(
                        unit -> {
                            fail("Expected errors to be returned");
                            return Unit.UNIT;
                        },
                        validationErrors -> {
                            assertThat(validationErrors.hasErrors()).isTrue();
                            return Unit.UNIT;
                        });

    }

}