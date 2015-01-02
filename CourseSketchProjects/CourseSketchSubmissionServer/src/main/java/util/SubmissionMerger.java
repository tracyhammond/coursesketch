package util;

import com.google.protobuf.InvalidProtocolBufferException;
import protobuf.srl.commands.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigemjt on 12/27/14.
 *
 * Merges two submissions with one coming from the database and the other coming from the client
 */
public class SubmissionMerger {
    private Commands.SrlUpdateList databaseList, clientList;
    private boolean isModerator;

    public SubmissionMerger(final Commands.SrlUpdateList databaseList, final Commands.SrlUpdateList clientList) {
        this.databaseList = databaseList;
        this.clientList = clientList;
    }

    /**
     * True if you are a moderator.
     * Moderators can only do certain things.
     * We do not check if you are an admin or a user as that is handled externally.
     *
     * @param isMod
     */
    public void setIsModerator(final boolean isMod) {
        this.isModerator = isMod;
    }

    /**
     * This merge ensures that the database list has no data lost.
     *
     * There are three cases:
     * <ul>
     * <li>An undo has happened after a save has happened.  This will insert a marker into an older part of the list.</li>
     * <li>A switch sketch occurs in the middle of another system. (which in case you must be the moderator or admin).</li>
     * <li>The client list is just longer than the database list.</li>
     * </ul>
     */
    public Commands.SrlUpdateList merge() throws MergeException {
        final List<Commands.SrlUpdate> result = merge(databaseList.getListList(), clientList.getListList());
        final Commands.SrlUpdateList.Builder build = Commands.SrlUpdateList.newBuilder();
        return build.addAllList(result).build();
    }

    private List<Commands.SrlUpdate> merge(final List<Commands.SrlUpdate> database, final List<Commands.SrlUpdate> client) throws MergeException {
        final int differentIndex = Checksum.indexOfDifference(database, client);
        if (differentIndex == -1) {
            return database;
        }

        // if the only difference is that one is longer than we return the entire client list as no modification has occurred.
        if (differentIndex == database.size() && !isModerator) {
            return client;
        } else if (isModerator) {
            final Commands.SrlUpdate differentUpdate = client.get(differentIndex);
            if (differentUpdate.getCommands(0).getCommandType() == Commands.CommandType.SWITCH_SKETCH
                    || differentUpdate.getCommands(0).getCommandType() == Commands.CommandType.CREATE_SKETCH) {
                // They just added the feedback at the end of the sketch.  We will allow moderators to perform this action.
                return client;
            }
            throw new MergeException("Moderator is not allowed to change the list in this way");
        }

        // something weird happened so we quit and don't modify.
        if (differentIndex == -2) {
            throw new MergeException("Client list can not be shorter than the database list! (someone is trying to change history)");
        }

        if (differentIndex == 0) {
            throw new MergeException("You can not override the create sketch");
        }

        // we have had a modification to the existing list and all other checks passed.
        final Commands.SrlUpdate differentUpdate = client.get(differentIndex);

        // switch sketch!
        if (differentUpdate.getCommands(0).getCommandType() == Commands.CommandType.SWITCH_SKETCH
                || differentUpdate.getCommands(0).getCommandType() == Commands.CommandType.CREATE_SKETCH) {
            final String startingSketch = getPreviousSketchId(database, differentIndex - 1);
            if (startingSketch == null) {
                throw new MergeException("Switch sketch inserted before parent sketch was created!");
            }
            final int endingIndex = getMatchingSketchId(client, differentIndex + 1, startingSketch);
            if (endingIndex == -1) {
                throw new MergeException("new additions do not switch back to the old sketch!");
            }
            final List<Commands.SrlUpdate> result = new ArrayList<>();
            final List<Commands.SrlUpdate> listForSecondMerge = new ArrayList<>();
            listForSecondMerge.addAll(client.subList(differentIndex, endingIndex + 1));
            listForSecondMerge.addAll(database.subList(differentIndex, database.size()));

            // takes in the already merged data and merges in the second half of the list
            final List<Commands.SrlUpdate> secondHalfOfMerge = merge(listForSecondMerge,
                    client.subList(differentIndex, client.size()));
            result.addAll(database.subList(0, differentIndex));
            //result.addAll(client.subList(differentIndex, endingIndex + 1));
            result.addAll(secondHalfOfMerge);
            return result;
        }

        // switch sketch!
        if (differentUpdate.getCommands(0).getCommandType() == Commands.CommandType.MARKER) {
            final Commands.SrlCommand command = differentUpdate.getCommands(0);
            Commands.Marker marker = null;
            try {
                marker = Commands.Marker.parseFrom(command.getCommandData());
            } catch (InvalidProtocolBufferException e) {
                throw new MergeException("List contains data in an invalid format.");
            }
            if (marker.getType() != Commands.Marker.MarkerType.SPLIT) {
                throw new MergeException("List contains an unsupported marker type");
            }
            final int endingIndex = Integer.parseInt(marker.getOtherData());
            if (endingIndex <= -1) {
                throw new MergeException("Split marker is not correctly formatted.");
            }

            if (endingIndex + differentIndex + 1 <= database.size()) {
                throw new MergeException("Split marker added at invalid position database list extends after marker");
            }

            if (isModerator) {
                throw new MergeException("Moderator is not allowed to change the list in this way");
            }

            final List<Commands.SrlUpdate> result = new ArrayList<>();
            result.addAll(database.subList(0, differentIndex));
            result.add(client.get(differentIndex));

            final List<Commands.SrlUpdate> merged = merge(database.subList(differentIndex, database.size()),
                    client.subList(differentIndex + 1, differentIndex + endingIndex));
            result.addAll(merged);
            result.addAll(client.subList(differentIndex + endingIndex, client.size()));
            return result;
        }
        return database;
    }

    /**
     * Goes back until a switch sketch or create sketch is found and returns that id.
     *
     * @param updates
     * @param startIndex
     * @return The id of the starting sketch.
     */
    private String getPreviousSketchId(final List<Commands.SrlUpdate> updates, final int startIndex) {
        for (int i = startIndex; i >= 0; i--) {
            final Commands.SrlCommand command = updates.get(i).getCommands(0);
            if (command.getCommandType() == Commands.CommandType.SWITCH_SKETCH) {
                try {
                    final Commands.IdChain id = Commands.IdChain.parseFrom(command.getCommandData());
                    return id.getIdChain(0);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            } else if (command.getCommandType() == Commands.CommandType.CREATE_SKETCH) {
                try {
                    final Commands.ActionCreateSketch createSketch = Commands.ActionCreateSketch.parseFrom(command.getCommandData());
                    return createSketch.getSketchId().getIdChain(0);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Goes back until a switch sketch or create sketch is found and returns that id.
     *
     * @param updates
     * @param startIndex
     * @return The id of the starting sketch.
     */
    private int getMatchingSketchId(final List<Commands.SrlUpdate> updates, final int startIndex, final String matchingSketchId) {
        for (int i = startIndex; i < updates.size(); i++) {
            final Commands.SrlCommand command = updates.get(i).getCommands(0);
            if (command.getCommandType() == Commands.CommandType.SWITCH_SKETCH) {
                try {
                    Commands.IdChain id = Commands.IdChain.parseFrom(command.getCommandData());
                    if (id.getIdChain(0).equals(matchingSketchId)) {
                        return i;
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            } else if (command.getCommandType() == Commands.CommandType.CREATE_SKETCH) {
                try {
                    final Commands.ActionCreateSketch createSketch = Commands.ActionCreateSketch.parseFrom(command.getCommandData());
                    if (createSketch.getSketchId().getIdChain(0).equals(matchingSketchId)) {
                        return i;
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }
}
