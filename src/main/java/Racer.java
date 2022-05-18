import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {
    public interface  Command extends Serializable {}

    public static class StartCommand  implements Command {
        private static final long serialVersionUID = 1L;
        private int raceLength;

        public StartCommand(int raceLength) {
            this.raceLength = raceLength;
        }

        public int getRaceLength() {
            return raceLength;
        }
    }

    public static class PositionCommand implements Command {
        private static final long serialVersionUID = 1L;
        private ActorRef<RaceController.Command> controller;

        public PositionCommand(ActorRef<RaceController.Command> controller) {
            this.controller = controller;
        }
        public ActorRef<RaceController.Command> getController() {
            return controller;
        }
    }
    private Racer(ActorContext context) {
        super(context);
    }
    public static Behavior<Command> create(){
        return Behaviors.setup(Racer::new);
    }

    private final double defaultAverageSpeed = 48.2;
    private int averageSpeedAdjustmentFactor;
    private Random random;



    private int raceLength;
    private double currentSpeed = 0;
    private double currentPosition = 0;
    private double getMaxSpeed() {
        return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed(int currentPosition, int raceLength) {
        if (currentPosition < (raceLength / 4)) {
            currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
        }
        else {
            currentSpeed = currentSpeed * (0.5 + random.nextDouble());
        }

        if (currentSpeed > getMaxSpeed())
            currentSpeed = getMaxSpeed();

        if (currentSpeed < 5)
            currentSpeed = 5;

        if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
            currentSpeed = getMaxSpeed() / 2;
        }
    }
    @Override
    public Receive<Command> createReceive() {
        return notYetStarted();
    }

    public Receive<Command> notYetStarted(){
        return newReceiveBuilder()
                .onMessage(StartCommand.class, message ->{
                    this.random = new Random();
                    this.averageSpeedAdjustmentFactor =random.nextInt(30) -10; //between -10 and 20
                    return running(message.getRaceLength(), 0); //at the start current position is zero
                })
                .onMessage(PositionCommand.class, message ->{
                    message.getController().tell(
                            new RaceController.RacerUpdateCommand(getContext().getSelf(), 0)
                    );
                    return Behaviors.same(); //We are not started yet
                })
                .build();
    }

    public Receive<Command> running(int raceLength, int currentPosition){
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, message ->{
                    determineNextSpeed(currentPosition, raceLength);
                    int newPosition = currentPosition;
                    newPosition += getDistanceMovedPerSecond();
                    if(newPosition>raceLength)
                        newPosition =raceLength;
                    //tell controller current position
                    message.getController().tell(
                            new RaceController.RacerUpdateCommand(getContext().getSelf(), (int)newPosition)
                    );
                    if (newPosition == raceLength){
                        return completed(raceLength);
                    }else {
                        return running(raceLength, newPosition);
                    }
                })
                .build();
    }

    public Receive<Command> completed(int raceLength){
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, message ->{
                    message.getController().tell(
                            new RaceController.RacerUpdateCommand(getContext().getSelf(), raceLength)
                    );
                    message.getController().tell(new RaceController.RacerFinishedCommand(getContext().getSelf()));
                    return Behaviors.ignore();
                })
                .build();
    }


}
