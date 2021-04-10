package net.johanbasson.fp.api.users;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.io.IO;

public interface UserRepository {

    IO<Maybe<User>> findByEmail(String email);

}
