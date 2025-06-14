package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;

//TODO: refactor to become FACTORY
public class FactoryEnv extends Environment {
    // action literals
    public static final Literal openTruck = Literal.parseLiteral("open(truck)");
    public static final Literal closeTruck = Literal.parseLiteral("close(truck)");
    public static final Literal getPackage = Literal.parseLiteral("get(package)");
    public static final Literal handInPackage = Literal.parseLiteral("hand_in(package)");
    public static final Literal takePackage = Literal.parseLiteral("take_item(package)");

    // belief literals
    public static final Literal hasDeliveryAPackage = Literal.parseLiteral("has(deliveryA,package)");
    public static final Literal atTruck = Literal.parseLiteral("at(robot,truck)");
    public static final Literal atDeliveryA = Literal.parseLiteral("at(robot,deliveryA)");

    static Logger logger = Logger.getLogger(FactoryEnv.class.getName());

    FactoryModel model; // the model of the grid

    @Override
    public void init(final String[] args) {
        this.model = new FactoryModel();

        if ((args.length == 1) && args[0].equals("gui")) {
            final FactoryView view = new FactoryView(this.model);
            this.model.setView(view);
        }
        // boot the agents' percepts
        this.updatePercepts();
    }

    /**
     * Update the agents' percepts based on current state of the environment
     * (HouseModel)
     */
    void updatePercepts() {
        // clear the percepts of the agents
        this.clearPercepts("robot");
        this.clearPercepts("deliveryA");

        // get the robot location
        final Location lRobot = this.model.getAgPos(0);

        // the robot can perceive where it is
        if (lRobot.equals(this.model.truckLocation)) {
            this.addPercept("robot", FactoryEnv.atTruck);
        }
        if (lRobot.equals(this.model.deliveryLocation)) {
            this.addPercept("robot", FactoryEnv.atDeliveryA);
        }

        // the robot can perceive the beer stock only when at the (open) fridge
        if (this.model.truckOpen) {
            this.addPercept(
                    "robot",
                    Literal.parseLiteral("stock(package,"
                            + "\"" + this.model.availablePackage + "\"" + ")"
                    )
            );
        }

        // the robot can perceive if the owner has beer (the owner too)
        if (this.model.itemCount > 0) {
            this.addPercept("robot", FactoryEnv.hasDeliveryAPackage);
            this.addPercept("deliveryA", FactoryEnv.hasDeliveryAPackage);
        }
    }

    /**
     * The <code>boolean</code> returned represents the action "feedback"
     * (success/failure)
     */
    @Override
    public boolean executeAction(final String ag, final Structure action) {
        System.out.println("[" + ag + "] doing: " + action);
        boolean result = false;
        if (action.equals(FactoryEnv.openTruck)) { // of = open(fridge)
            result = this.model.openTruck();
        } else if (action.equals(FactoryEnv.closeTruck)) { // clf = close(fridge)
            result = this.model.closeTruck();
        } else if (action.getFunctor().equals("move_towards")) {
            final String location = action.getTerm(0).toString(); // get where to move
            Location dest = null;
            if (location.equals("truck")) {
                dest = this.model.truckLocation;
            } else if (location.equals("deliveryA")) {
                dest = this.model.deliveryLocation;
            }
            result = this.model.moveTowards(dest);
        } else if (action.equals(FactoryEnv.getPackage)) { // gb = get(beer)
            result = this.model.getPackage();
        } else if (action.equals(FactoryEnv.handInPackage)) { // hb = hand_in(beer)
            result = this.model.deliverPackage();
        } else if (action.equals(FactoryEnv.takePackage)) { // sb = sip(beer)
            result = this.model.takeItem();
        } else if (action.getFunctor().equals("deliver")) {
            // simulate delivery time
            try {
                Thread.sleep(5_000);
                // randomly generate a package type (it can either be a, b, or c)
                String packageType = action.getTerm(1).toString().replaceAll("\"", "");
                String[] types = {"a"};
                packageType = types[(int) (Math.random() * types.length)];
                // add the package to the model
                result = this.model.addPackage(packageType);
                System.out.println("[" + ag + "] added package: " + packageType);
            } catch (final Exception e) {
                FactoryEnv.logger.info("Failed to execute action deliver!" + e);
            }
        } else {
            FactoryEnv.logger.info("Failed to execute action " + action);
        }
        // only if action completed successfully, update agents' percepts
        if (result) {
            this.updatePercepts();
            try {
                Thread.sleep(500);
            } catch (final Exception e) {
            }
        }
        return result;
    }
}
