package net.johanbasson.fp.api.users;

import com.jnape.palatable.lambda.adt.Either;
import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.io.IO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import net.johanbasson.fp.api.system.ApplicationContext;
import net.johanbasson.fp.api.system.errors.ApiError;
import net.johanbasson.fp.api.system.errors.ErrorType;
import net.johanbasson.fp.api.system.errors.ValidationErrors;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class Users {

    private static final Logger log = LoggerFactory.getLogger(Users.class);

    public static Fn1<ApplicationContext, IO<Either<ApiError, JwtToken>>> authenticate(AuthenticateUserCommand command) {
        return applicationContext -> {
            ValidationErrors validationErrors = command.validate();
            if (validationErrors.hasErrors()) {
                return IO.io(Either.left(validationErrors.toApiError()));
            }

            Function<String, IO<Maybe<User>>> fun1 = applicationContext.getUserRepository()::findByEmail;


            IO<Maybe<User>> byEmail = applicationContext.getUserRepository().findByEmail(command.email());
            return byEmail
                    .flatMap(maybeUser -> IO.io(
                            maybeUser.toEither(() -> ApiError.of(ErrorType.USER_NOT_FOUND))
                                    .flatMap(user -> checkUserAndGenerateToken(applicationContext, command.password(), user))
                            )
                    );
        };
    }

    public static Fn1<ApplicationContext, Either<ApiError, Principal>> authorize(String authHeader) {
        return applicationContext -> {
            if (StringUtils.isEmpty(authHeader)) {
                return Either.left(ApiError.of(ErrorType.INVALID_AUTH_HEADER));
            }
            String token = authHeader.startsWith("BEARER") ? authHeader.substring(7).trim() : authHeader.trim();
            try {
                Jws<Claims> jws = Jwts.parserBuilder()
                        .setSigningKey(applicationContext.getSecretKey())
                        .build()
                        .parseClaimsJws(token);
                return Either.right(Principal.from(jws.getBody()));
            } catch (Exception ex) {
                log.error("Unable to parse token", ex);
                return Either.left(ApiError.of(ErrorType.INVALID_JWT_TOKEN));
            }
        };
    }

    private static Either<ApiError, JwtToken> checkUserAndGenerateToken(ApplicationContext ctx, String password, User user) {
        if (BCrypt.checkpw(password, user.password())) {
            String token = Jwts.builder()
                    .setSubject(user.id().toString())
                    .claim("ROLE", user.role().name())
                    .signWith(ctx.getSecretKey()).compact();
            return Either.right(new JwtToken(token, 1000)); // TODO Implement timeout
        } else {
            return Either.left(ApiError.of(ErrorType.PASSWORDS_DOES_NOT_MATCH));
        }
    }

}
