import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    public interface  Command extends Serializable {}
    private RaceController(ActorContext context) {
        super(context);
    }

    public static Behavior create(){
        return Behaviors.setup(RaceController::new);
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
