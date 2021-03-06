package cs425.mp4.crane.Topology;

import java.io.Serializable;

/**
 * Helper class. Record all necessary information about a spout/bolt.
 */
public class TopologyRecord implements Serializable {
    public static final char spoutType='s';
    public static final char boltType='b';
    public final String id;
    public final char type;
    public final Serializable operationUnit;
    public final int numTasks;
    public final String parentID;
    public final String groupingField;

    public TopologyRecord(String id, char type, Serializable operationUnit, int numTasks,
                          String dependency, String groupingField) {
        this.id = id;
        this.type = type;
        this.operationUnit = operationUnit;
        this.numTasks = numTasks;
        this.parentID = dependency;
        this.groupingField = groupingField;
    }
}
