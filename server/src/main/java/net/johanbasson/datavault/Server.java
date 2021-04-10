package net.johanbasson.datavault;

import io.javalin.Javalin;
import net.johanbasson.fp.api.system.ApplicationContext;
import net.johanbasson.fp.api.users.AuthenticateUserCommand;
import net.johanbasson.fp.api.users.Users;

import static net.johanbasson.datavault.ContextUtil.ok;

public class Server {

    private final ApplicationContext applicationContext;
    private final Javalin application;

    public Server(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        application = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
        });

        application.post("/authenticate", context -> {
            AuthenticateUserCommand command = context.bodyAsClass(AuthenticateUserCommand.class);
            context.result(
                    Users.authenticate(command)
                            .apply(applicationContext)
                            .unsafePerformAsyncIO()
                            .handle(ok(context))
            );
        });

    }

    public void start() {
        application.start(applicationContext.getConfiguration().server().port());
    }

    public void stop() {
        application.stop();
    }

}
