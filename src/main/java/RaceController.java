
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    public interface Command extends Serializable {}

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public static class RacerUpdateCommand implements Command{
        private static final long serialVersionUID = 1L;
        private ActorRef<Racer.Command> racer;
        private int position;

        public RacerUpdateCommand(ActorRef<Racer.Command> racer, int position) {
            this.racer = racer;
            this.position = position;
        }

        public ActorRef<Racer.Command> getRacer() {
            return racer;
        }

        public int getPosition() {
            return position;
        }
    }

    private RaceController(ActorContext context) {
        super(context);
    }

    public static Behavior<Command> create(){
        return Behaviors.setup(RaceController::new);
    }

    private Map<ActorRef<Racer.Command>, Integer> currentPositions;
    private long start;
    private int racerLength = 100;
    @Override
    public Receive<Command> createReceive() {

        return newReceiveBuilder()
                .onMessage(StartCommand.class, message ->{
                    start = System.currentTimeMillis();
                    currentPositions= new HashMap<>();
                    for (int i = 0; i < 10; i++) {
                        ActorRef<Racer.Command> racer = getContext().spawn(Racer.create(), "racer" + i);
                        currentPositions.put(racer,0); //Postion is at zero at start of the race
                        racer.tell(new Racer.StartCommand(racerLength));
                    }
                    return this;
                })
                .onMessage(RacerUpdateCommand.class, mesage->{
                    currentPositions.put(mesage.getRacer(), mesage.getPosition());
                    return this;
                })
                .build();
    }
}
