package util;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import utilities.LoggingConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigemjt on 12/27/14.
 *
 * Merges two submissions with one coming from the database and the other coming from the client
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity" })
public final class SubmissionMerger {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionMerger.class);

    /**
     * The lists that are being merged together.
     */
    private final Commands.SrlUpdateList databaseList, clientList;

    /**
     * True if the user is merging as a moderator.
     *
     * Moderator can only merge lists in certain ways.
     */
    private boolean isModerator;

    /**
     * Creates an instance with the two lists that are going to be merged.
     *
     * @param existingList
     *         The original reference list that is stored in the existingList.
     * @param mergeInList
     *         The new client list that is being stored in the .
     */
    public SubmissionMerger(final Commands.SrlUpdateList existingList, final Commands.SrlUpdateList mergeInList) {
        this.databaseList = existingList;
        this.clientList = mergeInList;
    }

    /**
     * True if you are a moderator.
     * Moderators can only do certain things.
     * We do not check if you are an admin or a user as that is handled externally.
     *
     * @param isMod
     *         true if the submission is being merged as a moderator.
     * @return itself.
     */
    public SubmissionMerger setIsModerator(final boolean isMod) {
        this.isModerator = isMod;
        return this;
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
     *
     * @return {@link protobuf.srl.commands.Commands.SrlUpdateList} The result will be a merged list.
     * If the merge fails then an exception is thrown instead of returning a value.
     * @throws MergeException
     *         Thrown if there is an issue while merging.
     *         The user should assume that the merge was aborted and instead use the database list
     */
    public Commands.SrlUpdateList merge() throws MergeException {
        final List<Commands.SrlUpdate> result = merge(databaseList.getListList(), clientList.getListList());
        final Commands.SrlUpdateList.Builder build = Commands.SrlUpdateList.newBuilder();
        return build.addAllList(result).build();
    }

    /**
     * Merges the two lists together recursively.
     *
     * @param database
     *         The database list
     * @param client
     *         The new list that is being merged in.
     * @return A merged form of the list.
     * @throws MergeException
     *         Thrown if the merge fails.  One should reject the client list if this exception is thrown.
     */
    private List<Commands.SrlUpdate> merge(final List<Commands.SrlUpdate> database, final List<Commands.SrlUpdate> client) throws MergeException {
        final int differentIndex = Checksum.indexOfDifference(database, client);
        if (differentIndex == -1) {
            // we return database in-case the change is actually a cheater induced conflict of hashing.
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
        if (differentIndex == Checksum.WRONG_LIST_SIZE_ERROR) {
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
            return mergeSwitchSketch(database, client, differentIndex);
        }

        // switch sketch!
        if (differentUpdate.getCommands(0).getCommandType() == Commands.CommandType.MARKER) {
            return mergeMarker(database, client, differentUpdate, differentIndex);
        }
        throw new MergeException("Merge type is not supported, merge failed.");
    }

    /**
     * Merges in the list that occurs when the difference is a marker.
     *
     * @param database
     *         The database list.
     * @param client
     *         The new list that is being merged in.
     * @param differentUpdate
     *         the update that is different in the client list.
     * @param differentIndex
     *         The index at which the two list diverge.
     * @return A list that represents to the two merged versions.
     * @throws MergeException
     *         Thrown if the merge fails.  One should reject the client list if this exception is thrown.
     */
    private List<Commands.SrlUpdate> mergeMarker(final List<Commands.SrlUpdate> database, final List<Commands.SrlUpdate> client,
            final Commands.SrlUpdate differentUpdate,
            final int differentIndex) throws MergeException {
        final Commands.SrlCommand firstCommand = differentUpdate.getCommands(0);
        Commands.Marker mark = null;
        try {
            mark = Commands.Marker.parseFrom(firstCommand.getCommandData());
        } catch (InvalidProtocolBufferException e) {
            throw new MergeException("Command of marker type does not actually contain marker", e);
        }
        if (mark.getType() == Commands.Marker.MarkerType.SPLIT) {
            return mergeRedoUndo(database, client, differentIndex, mark);
        }
        if (mark.getType() == Commands.Marker.MarkerType.SUBMISSION || mark.getType() == Commands.Marker.MarkerType.SAVE) {
            return mergeTimeChange(database, client, differentUpdate, differentIndex);
        }
        throw new MergeException("Merge can not successfully be completed");
    }

    /**
     * Merges in the list that occurs when the difference is a possible time change in the marker.
     *
     * @param database
     *         The database list.
     * @param client
     *         The new list that is being merged in.
     * @param clientUpdate
     *         the update that is different in the client list.
     * @param differentIndex
     *         The index at which the two list diverge.
     * @return A list that represents to the two merged versions.
     * @throws MergeException
     *         Thrown if the merge fails.  One should reject the client list if this exception is thrown.
     */
    private List<Commands.SrlUpdate> mergeTimeChange(final List<Commands.SrlUpdate> database, final List<Commands.SrlUpdate> client,
            final Commands.SrlUpdate clientUpdate, final int differentIndex) throws MergeException {
        final Commands.SrlUpdate databaseUpdate = database.get(differentIndex);
        final Commands.SrlCommand clientCommand = clientUpdate.getCommands(0);
        final Commands.SrlCommand databaseCommand = databaseUpdate.getCommands(0);
        if (!databaseCommand.equals(clientCommand)) {
            throw new MergeException("Commands must be equal to merge time differences only");
        }
        if (clientUpdate.getTime() == databaseUpdate.getTime()) {
            // prevents possible infinite loops
            throw new MergeException("Only difference between updates can be the time they took place");
        }
        final Commands.SrlUpdate.Builder clientReplacement = Commands.SrlUpdate.newBuilder(clientUpdate);
        clientReplacement.setTime(databaseUpdate.getTime());
        final List<Commands.SrlUpdate> updatedClientList = new ArrayList<>();
        updatedClientList.addAll(client.subList(0, differentIndex));
        updatedClientList.add(clientReplacement.build());
        updatedClientList.addAll(client.subList(differentIndex + 1, client.size()));
        return merge(database, updatedClientList);
    }

    /**
     * Merges in the list that occurs when a switch sketch is added in the middle.
     *
     * @param database
     *         The database list.
     * @param client
     *         The new list that is being merged in.
     * @param differentIndex
     *         The index at which the two list diverge.
     * @return A list that represents to the two merged versions.
     * @throws MergeException
     *         Thrown if the merge fails.  One should reject the client list if this exception is thrown.
     */
    private List<Commands.SrlUpdate> mergeSwitchSketch(final List<Commands.SrlUpdate> database, final List<Commands.SrlUpdate> client,
            final int differentIndex) throws MergeException {
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

    /**
     * Merges in the list that occurs when a switch sketch is added in the middle.
     *
     * @param database
     *         The database list.
     * @param client
     *         The new list that is being merged in.
     * @param differentIndex
     *         The index at which the two list diverge.
     * @param marker The marker that contains the split data.
     * @return A list that represents to the two merged versions.
     * @throws MergeException
     *         Thrown if the merge fails.  One should reject the client list if this exception is thrown.
     */
    private List<Commands.SrlUpdate> mergeRedoUndo(final List<Commands.SrlUpdate> database, final List<Commands.SrlUpdate> client,
            final int differentIndex, final Commands.Marker marker) throws MergeException {
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

    /**
     * Goes back until a switch sketch or create sketch is found and returns that id.
     *
     * @param updates
     *         the list of updates to search through.
     * @param startIndex
     *         The index to search from
     * @return The id of the starting sketch.
     */
    private String getPreviousSketchId(final List<Commands.SrlUpdate> updates, final int startIndex) {
        for (int i = startIndex; i >= 0; i--) {
            final Commands.SrlCommand command = updates.get(i).getCommands(0);
            if (command.getCommandType() == Commands.CommandType.SWITCH_SKETCH) {
                try {
                    final Commands.IdChain sketchId = Commands.IdChain.parseFrom(command.getCommandData());
                    return sketchId.getIdChain(0);
                } catch (InvalidProtocolBufferException e) {
                    LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            } else if (command.getCommandType() == Commands.CommandType.CREATE_SKETCH) {
                try {
                    final Commands.ActionCreateSketch createSketch = Commands.ActionCreateSketch.parseFrom(command.getCommandData());
                    return createSketch.getSketchId().getIdChain(0);
                } catch (InvalidProtocolBufferException e) {
                    LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            }
        }
        return null;
    }

    /**
     * Goes back until a switch sketch or create sketch is found and returns that id.
     *
     * @param updates
     *         The list of updates to search through
     * @param startIndex
     *         the index to search from
     * @param matchingSketchId
     *         the Id that we are trying to find the indexof.
     * @return The id of the starting sketch.
     */
    private int getMatchingSketchId(final List<Commands.SrlUpdate> updates, final int startIndex, final String matchingSketchId) {
        for (int i = startIndex; i < updates.size(); i++) {
            final Commands.SrlCommand command = updates.get(i).getCommands(0);
            if (command.getCommandType() == Commands.CommandType.SWITCH_SKETCH) {
                try {
                    final Commands.IdChain sketchId = Commands.IdChain.parseFrom(command.getCommandData());
                    if (sketchId.getIdChain(0).equals(matchingSketchId)) {
                        return i;
                    }
                } catch (InvalidProtocolBufferException e) {
                    LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            } else if (command.getCommandType() == Commands.CommandType.CREATE_SKETCH) {
                try {
                    final Commands.ActionCreateSketch createSketch = Commands.ActionCreateSketch.parseFrom(command.getCommandData());
                    if (createSketch.getSketchId().getIdChain(0).equals(matchingSketchId)) {
                        return i;
                    }
                } catch (InvalidProtocolBufferException e) {
                    LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            }
        }
        return -1;
    }
}
