import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {
        ActorSystem<RaceController.Command> raceControler = ActorSystem.create(RaceController.create(), "RaceSimulation");
        raceControler.tell(new RaceController.StartCommand());
    }
}
