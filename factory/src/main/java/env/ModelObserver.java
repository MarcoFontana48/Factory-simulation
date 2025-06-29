package env;

import jason.environment.grid.Location;

/**
* ModelObserver interface for observing changes in the FactoryModel.
* Observers can implement this interface to receive updates on agent movements and cell updates.
*/
public interface ModelObserver {
    void onAgentUpdated(Location location, int agentId);
    void onAgentMoved(Location oldLocation, Location newLocation, int agentId);
    void onCellUpdated(Location location);
}
