package util;

import com.google.protobuf.ByteString;
import org.junit.Assert;
import org.junit.Test;
import protobuf.srl.commands.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gigemjt on 12/28/14.
 */
public class SubmissionMergerTest {
    private static final String sketch1Id = "Sketch1";
    private static final String sketch2Id = "Sketch2";
    public static Commands.SrlUpdateList createSimpleDatabaseList(long time) {
        Commands.SrlUpdateList.Builder result = Commands.SrlUpdateList.newBuilder();
        result.addList(makeNewUpdateFromCommands("Update1", time, makeNewSketchCommand("CreateSketch1", sketch1Id, false)));
        result.addList(makeNewUpdateFromCommands("Update2", time + 10, makeNewCommand("Stroke1", Commands.CommandType.ADD_STROKE, null)));
        result.addList(makeNewUpdateFromCommands("Update3", time + 20, makeNewCommand("Stroke2", Commands.CommandType.ADD_STROKE, null),
                makeNewCommand("Stroke3", Commands.CommandType.ADD_STROKE, null)));
        result.addList(makeNewUpdateFromCommands("Update4", time + 30, makeNewCommand("Clear1", Commands.CommandType.CLEAR, null),
                makeNewCommand("Stroke4", Commands.CommandType.ADD_STROKE, null)));
        result.addList(makeNewUpdateFromCommands("Update5", time + 40, makeNewCommand("Clear2", Commands.CommandType.CLEAR, null)));
        return result.build();
    }

    private static Commands.SrlUpdate makeNewUpdateFromCommands(String id, long time, Commands.SrlCommand... commands) {
        return Commands.SrlUpdate.newBuilder().setUpdateId(id).setTime(time).addAllCommands(Arrays.asList(commands)).build();
    }

    private static Commands.SrlCommand makeNewSketchCommand(String commandId, String sketchId, final boolean useSwitchSketch) {
        Commands.SrlCommand.Builder command = Commands.SrlCommand.newBuilder();
        command.setCommandType(useSwitchSketch ? Commands.CommandType.SWITCH_SKETCH : Commands.CommandType.CREATE_SKETCH);
        command.setCommandId(commandId);
        command.setCommandData(Commands.ActionCreateSketch.newBuilder().setSketchId(
                Commands.IdChain.newBuilder().addIdChain(sketchId)).build().toByteString());
        command.setIsUserCreated(true);
        return command.build();
    }

    private static Commands.SrlCommand makeNewCommand(String commandId, Commands.CommandType type, ByteString data) {
        Commands.SrlCommand.Builder command = Commands.SrlCommand.newBuilder();
        command.setCommandType(type);
        command.setCommandId(commandId);
        command.setIsUserCreated(true);
        if (data != null) {
            command.setCommandData(data);
        }
        return command.build();
    }

    /**
     * Inserts a different sketch with its own updates into the given list.
     * @param index
     * @param useSwitchSketch
     * @return
     */
    public static Commands.SrlUpdateList createSimpleDatabaseListInsertSketchAt(Commands.SrlUpdateList list, int index, long time,
            final boolean useSwitchSketch) {
        List<Commands.SrlUpdate> updates = list.getListList();
        List<Commands.SrlUpdate> firstHalf = updates.subList(0 , index);
        List<Commands.SrlUpdate> secondHalf = updates.subList(index, updates.size());
        List<Commands.SrlUpdate> middle = new ArrayList<>();

        middle.add(makeNewUpdateFromCommands("Update6", time + 50, makeNewSketchCommand("CreateSketch2", sketch2Id, useSwitchSketch)));
        middle.add(makeNewUpdateFromCommands("Update7", time + 60, makeNewCommand("Stroke4", Commands.CommandType.ADD_STROKE, null)));
        middle.add(makeNewUpdateFromCommands("Update8", time + 70, makeNewCommand("Shape1", Commands.CommandType.ADD_SHAPE, null),
                makeNewCommand("Clear3", Commands.CommandType.CLEAR, null)));

        Commands.IdChain idChain= Commands.IdChain.newBuilder().addIdChain(sketch1Id).build();
        middle.add(makeNewUpdateFromCommands("Update9", time + 80, makeNewCommand("Switch1", Commands.CommandType.SWITCH_SKETCH,
                idChain.toByteString())));
        Commands.SrlUpdateList.Builder result = Commands.SrlUpdateList.newBuilder();
        result.addAllList(firstHalf);
        result.addAllList(middle);
        result.addAllList(secondHalf);
        return result.build();
    }

    /**
     * Inserts a different sketch with its own updates into the given list.
     * @param startIndex
     * @return
     */
    public static Commands.SrlUpdateList createSimpleDatabaseListInsertMarkerAt(Commands.SrlUpdateList list, int startIndex, int endIndex, long time) {
        List<Commands.SrlUpdate> updates = list.getListList();
        Commands.SrlUpdateList.Builder result = Commands.SrlUpdateList.newBuilder();
        List<Commands.SrlUpdate> firstHalf = updates.subList(0 , startIndex);
        result.addAllList(updates.subList(0 , startIndex));

        List<Commands.SrlUpdate> secondHalf = updates.subList(startIndex, updates.size());
        int splitSize = secondHalf.size();
        List<Commands.SrlUpdate> afterUpdate = new ArrayList<>();
        for (int i = 0; i < secondHalf.size(); i++) {
            afterUpdate.add(makeNewUpdateFromCommands("UndoUpdate" + i, time + 50 * (i + 1), makeNewCommand("UndoCom" + i, Commands.CommandType.UNDO, null)));
            splitSize += 1;
        }
        Commands.Marker marker = Commands.Marker.newBuilder().setOtherData("" + ((endIndex - startIndex) * 2)).setType(
                Commands.Marker.MarkerType.SPLIT).build();
        result.addList(makeNewUpdateFromCommands("UpdateSplit1", time + 50, makeNewCommand("Split1", Commands.CommandType.MARKER, marker.toByteString())));

        result.addAllList(secondHalf);
        result.addAllList(afterUpdate);
        result.addList(makeNewUpdateFromCommands("UpdateSplit2", time + 50, makeNewCommand("Split2", Commands.CommandType.MARKER, marker.toByteString())));

        List<Commands.SrlUpdate> middle = new ArrayList<>();

        middle.add(makeNewUpdateFromCommands("Update6", time + 50, makeNewCommand("Stroke4", Commands.CommandType.ADD_STROKE, null)));
        middle.add(makeNewUpdateFromCommands("Update7", time + 60, makeNewCommand("Stroke4", Commands.CommandType.ADD_STROKE, null)));
        middle.add(makeNewUpdateFromCommands("Update8", time + 70, makeNewCommand("Shape1", Commands.CommandType.ADD_SHAPE, null),
                makeNewCommand("Clear3", Commands.CommandType.CLEAR, null)));

        result.addAllList(middle);
        return result.build();
    }

    @Test
    public void noChangeIfBothListAreTheSame() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        SubmissionMerger merger = new SubmissionMerger(list1, list1);
        Commands.SrlUpdateList list2 = merger.merge();
        Assert.assertEquals(list1, list2);
    }

    @Test
    public void ifOnlyChangeIsAdditionThenNewListIsReturned() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder list2 = Commands.SrlUpdateList.newBuilder(list1);
        list2.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom", Commands.CommandType.CLEAR, null)));
        SubmissionMerger merger = new SubmissionMerger(list1, list2.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2.build(), list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test(expected = MergeException.class)
    public void ifOnlyChangeIsAdditionAndModeratorThenException() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder list2 = Commands.SrlUpdateList.newBuilder(list1);
        list2.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom", Commands.CommandType.CLEAR, null)));
        SubmissionMerger merger = new SubmissionMerger(list1, list2.build());
        merger.setIsModerator(true);
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2.build(), list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test
    public void ifChangeIsSaveMarkerTimeDatabaseTimeIsUsesSave() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        Commands.Marker mark = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SAVE).build();
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 200, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test(expected = MergeException.class)
    public void exceptionThrownIfCommandsAreNotEqual() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        Commands.Marker mark = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SAVE).build();
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 200, makeNewCommand("HAX_COMMAND",
                Commands.CommandType.MARKER, mark.toByteString())));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test
    public void ifChangeIsSaveMarkerTimeDatabaseTimeIsUsesSubmit() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        Commands.Marker mark = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SUBMISSION).build();
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 200, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test(expected = MergeException.class)
    public void testChangeMarkerThrowsExceptionIfMarkerIsNotChange() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        Commands.Marker mark = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.FEEDBACK).build();
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 200, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test(expected = MergeException.class)
    public void exceptionThrownIfMergeTypeNotSupported() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.ADD_SHAPE, null)));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.ADD_STROKE, null)));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test(expected = MergeException.class)
    public void testChangeMarkerThrowsExceptionIfUpdateIsDifferent() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        Commands.Marker mark = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SAVE).build();
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 200, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString()), makeNewCommand("CHEATY COM!",
                Commands.CommandType.MARKER, mark.toByteString())));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test(expected = MergeException.class)
    public void testChangeMarkerThrowsExceptionIfNonMarkerDataIsStoredInMarker() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder databaseList = Commands.SrlUpdateList.newBuilder(list1);
        Commands.SrlCommand mark = makeNewCommand("FAKE COMMAND", Commands.CommandType.MARKER, null);
        databaseList.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        Commands.SrlUpdateList.Builder client = Commands.SrlUpdateList.newBuilder(list1);
        client.addList(makeNewUpdateFromCommands("NewUpdate1", time + 200, makeNewCommand("NewCom",
                Commands.CommandType.MARKER, mark.toByteString())));
        SubmissionMerger merger = new SubmissionMerger(databaseList.build(), client.build());
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(databaseList.build(), list3);
        Assert.assertNotEquals(client.build(), list3);
    }

    @Test(expected = MergeException.class)
    public void ifChangeIsDeletionExceptionIsThrown() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList.Builder list2 = Commands.SrlUpdateList.newBuilder(list1);
        list2.addList(makeNewUpdateFromCommands("NewUpdate1", time + 100, makeNewCommand("NewCom", Commands.CommandType.CLEAR, null)));
        SubmissionMerger merger = new SubmissionMerger(list2.build(), list1);
        Commands.SrlUpdateList list3 = merger.merge();
    }

    @Test()
    public void switchSketchMergesCorrectlyAddedAtEnd() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, list1.getListCount(), time, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
    public void switchSketchMergesCorrectlyAddedAtEndAsModerator() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, list1.getListCount(), time, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        merger.setIsModerator(true);
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
    public void CreateSketchMergesCorrectlyAddedAtEndAsModerator() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, list1.getListCount(), time, false);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        merger.setIsModerator(true);
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
    public void multipleSwitchSketchMergesCorrectly() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList listTemp = createSimpleDatabaseListInsertSketchAt(list1, list1.getListCount(), time, true);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(listTemp, 2, time + 100, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
    public void multipleSwitchSketchMergesCorrectly2() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList listTemp = createSimpleDatabaseListInsertSketchAt(list1, 4, time, true);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(listTemp, 2, time + 100, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
     public void createSketchMergesCorrectlyAddedInMiddle() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, 2, time, false);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        // Assert.assertEquals(list1, list2); // used to see differences between list
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
    public void switchSketchMergesCorrectlyAddedInMiddle() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, 2, time, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        // Assert.assertEquals(list1, list2); // used to see differences between list
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test()
    public void markerMergesCorrectlyAddedInMiddle() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertMarkerAt(list1, 2, 4, time);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        // Assert.assertEquals(list1, list2); // used to see differences between list
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test(expected = MergeException.class)
    public void switchSketchMergesFailsAddedAtFront() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, 0, time, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
    }

    @Test()
    public void switchSketchMergesCorrectlyAddedAtRightAfterCreationOfSketch() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList list2 = createSimpleDatabaseListInsertSketchAt(list1, 1, time, true);
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
        // Assert.assertEquals(list1, list2); // used to see differences between list
        Assert.assertEquals(list2, list3);
        Assert.assertNotEquals(list1, list3);
    }

    @Test(expected = MergeException.class)
    public void switchSketchThrowsErrorIfSketchIsNotSwitchedBack() throws MergeException {
        long time = System.currentTimeMillis();
        Commands.SrlUpdateList list1 = createSimpleDatabaseList(time);
        Commands.SrlUpdateList listTemp = createSimpleDatabaseListInsertSketchAt(list1, 2, time, true);
        Commands.SrlUpdateList list2 = Commands.SrlUpdateList.newBuilder().addAllList(
                listTemp.getListList().subList(0, 5)).addAllList(listTemp.getListList().subList(6, listTemp.getListCount())).build();
        SubmissionMerger merger = new SubmissionMerger(list1, list2);
        Commands.SrlUpdateList list3 = merger.merge();
    }
}
