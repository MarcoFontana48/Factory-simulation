package env;

public class FactoryUtils {
    /**
     * Retrieves a delivery robot by its name.
     * @param robotName the name of the delivery robot
     * @return the DeliveryRobot with the specified name, or null if not found
     */
    public static int getAgIdBasedOnName(String agName) {
        if (agName == null) {
            return -1; // return -1 for null names
        }
        return switch (agName) {
            case "d_bot_1" -> 0;
            case "d_bot_2" -> 1;
            case "d_bot_3" -> 2;
            case "d_bot_4" -> 3;
            case "d_bot_5" -> 4;
            case "ch_st_1" -> 5;
            case "ch_st_2" -> 6;
            case "ch_st_3" -> 7;
            case "truck_1" -> 8;
            case "deliv_A" -> 9;
            case "humn_1" -> 10;
            default -> -1;
        };
    }

    /**
     * Retrieves the name of an agent based on its ID.
     * @param agentId the ID of the agent
     * @return the name of the agent, or "unknown" if the ID is not recognized
     */
    public static String getAgNameBasedOnId(int agentId) {
        return switch (agentId) {
            case 0 -> "d_bot_1";
            case 1 -> "d_bot_2";
            case 2 -> "d_bot_3";
            case 3 -> "d_bot_4";
            case 4 -> "d_bot_5";
            case 5 -> "ch_st_1";
            case 6 -> "ch_st_2";
            case 7 -> "ch_st_3";
            case 8 -> "truck_1";
            case 9 -> "deliv_A";
            case 10 -> "humn_1";
            default -> "unknown";
        };
    }
}
