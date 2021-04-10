package net.johanbasson.fp.api.workspace;

import com.jnape.palatable.lambda.adt.Either;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.io.IO;
import net.johanbasson.fp.api.system.ApplicationContext;
import net.johanbasson.fp.api.system.Created;
import net.johanbasson.fp.api.system.errors.ApiError;
import net.johanbasson.fp.api.system.errors.ErrorType;
import net.johanbasson.fp.api.types.Description;
import net.johanbasson.fp.api.types.Identifier;
import net.johanbasson.fp.api.types.Name;
import net.johanbasson.fp.api.users.Principal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.johanbasson.fp.api.system.errors.ValidationUtil.validate;

public final class Workspaces {


    public static Fn1<ApplicationContext, IO<Either<ApiError, Created>>> create(Principal principal, Name name, Description description) {
        return context -> validate(
                Arrays.asList(
                        Tuple2.tuple("Principal", principal),
                        Tuple2.tuple("Name", name),
                        Tuple2.tuple("Description", description)
                ))
                .toEither(() -> Unit.UNIT)
                .match(
                        unit -> context.getWorkspaceRepository().findByName(principal, name)
                                .flatMap(maybeWorkspace -> {
                                    Either<ApiError, Created> res = maybeWorkspace
                                            .toEither(Identifier::generate)
                                            .invert()
                                            .match(
                                                    workspace -> Either.left(ApiError.of(ErrorType.WORKSPACE_ALREADY_EXISTS)),
                                                    id -> {
                                                        Created created = new Created(id);
                                                        context.getCommandBus().execute(new CreateWorkspaceCommand(principal, name, description, id));
                                                        return Either.right(created);
                                                    });
                                    return IO.io(res);
                                }),
                        validationErrors -> IO.io(Either.left(validationErrors.toApiError()))
                );
    }

    public static Fn1<ApplicationContext, IO<Either<ApiError, List<Workspace>>>> list(Principal principal) {
        return context -> validate(Collections.singletonList(Tuple2.tuple("Principal", principal)))
                .toEither(() -> Unit.UNIT)
                .match(
                        unit -> context.getWorkspaceRepository().list(principal).fmap(Either::right),
                        validationErrors -> IO.io(Either.left(validationErrors.toApiError()))
                );
    }

}
