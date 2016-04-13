package com.coursesketch.test.utilities;

import com.coursesketch.test.utilities.ProtobufComparison;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.commands.Commands;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;

/**
 * Created by gigemjt on 9/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtobufComparisonTest {
    @Test
    public void testComparisonOfSameProtoObjects() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.Request.getDefaultInstance(), Message.Request.getDefaultInstance());
    }

    @Test(expected = AssertionError.class)
    public void testComparisonOfSameProtoObjectsDiffValues() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.Request.newBuilder().setRequestId("6").buildPartial(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfSameProtoObjectsDiffValuesButItIsIgnored() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().ignoreField(Message.Request.getDescriptor().findFieldByName("requestId")).build();
        comp.equals(Message.Request.newBuilder().setRequestId("6").buildPartial(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfDefaultValuesAreIgnore() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().ignoreField(Message.Request.getDescriptor().findFieldByName("requestId")).build();
        comp.equals(Message.Request.newBuilder().setRequestId("").buildPartial(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfSameProtoObjects2() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.Request.getDefaultInstance(), Message.Request.getDefaultInstance());
    }

    @Test(expected = AssertionError.class)
    public void testComparisonOfSameProtoObjectsThrowsException() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.LoginInformation.getDefaultInstance(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testListsWithSameValuesButDifferentOrderIgnoreOrder() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setIgnoreListOrder(true).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + (10 - i));
        }
        comp.equals(builder1.build(), builder2.build());
    }

    @Test
    public void testListsWithSameValuesSameOrder() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(false).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + i);
        }
        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testListsWithExtraItemsIgnoreOrder() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setIgnoreListOrder(true).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + (10 - i));
        }
        builder2.addItemId("12");
        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testListsWithSameValuesSameOrderExtraItems() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(false).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + i);
        }

        builder2.addItemId("" + 11);

        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testListsWithSameValuesSameOrderMissingItems() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(false).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + i);
        }

        builder1.addItemId("" + 11);

        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testListsWithSameValuesIgnoreOrderMissingItems() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(true).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + i);
        }

        builder1.addItemId("" + 11);

        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testListsWithSameValuesButDifferentOrder() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(false).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + (10 - i));
        }
        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testListsSomeSimilarValuesButSomeDifferentValues() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(true).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
            builder2.addItemId("" + (11 - i));
        }
        comp.equals(builder1.build(), builder2.build());
    }

    @Test
    public void testDeepEqualsDoesNotFaillInstantlyWithIgnoreListOrder() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setIgnoreListOrder(true).build();
        final Commands.SrlUpdateList.Builder updateList1 = Commands.SrlUpdateList.newBuilder();
        final Commands.SrlUpdateList.Builder updateList2 = Commands.SrlUpdateList.newBuilder();

        for (int i = 0; i <= 10; i++) {
            updateList1.addList(Commands.SrlUpdate.newBuilder().setTime(i* 30).setUpdateId("" + i));
        }
        updateList2.addAllList(Lists.reverse(updateList1.getListList()));
        comp.equals(updateList1.build(), updateList2.build());
    }

    @Test(expected = AssertionError.class)
    public void testOnlyExpectedListSet() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(true).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder1.addItemId("" + i);
        }
        comp.equals(builder1.build(), builder2.build());
    }

    @Test(expected = AssertionError.class)
    public void testOnlyActualListSet() {
        ProtobufComparison comp = new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .setIgnoreListOrder(true).build();
        final Data.ItemRequest.Builder builder1 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);
        final Data.ItemRequest.Builder builder2 = Data.ItemRequest.newBuilder().setQuery(Data.ItemQuery.ASSIGNMENT);

        for (int i = 0; i <= 10; i++) {
            builder2.addItemId("" + i);
        }
        comp.equals(builder1.build(), builder2.build());
    }
}
