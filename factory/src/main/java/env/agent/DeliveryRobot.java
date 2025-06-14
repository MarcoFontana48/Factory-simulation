package env.agent;

public class DeliveryRobot {
    private int id;
    private int energy;

    public DeliveryRobot(int id) {
        this.id = id;
        this.energy = 100; // initial energy level
    }

    public int getId() {
        return id;
    }

    public int getEnergy() {
        return energy;
    }

    public void decreaseEnergy(int amount) {
        energy -= amount;
        if (energy < 0) {
            energy = 0;
        }
    }
}
