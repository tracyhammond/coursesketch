package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import database.DatabaseClient;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.submission.Submission.SrlChecksum;

public class ChecksumTester {
	final File DEFAULT_DRAWING = new File("resources/tst/testDrawing.dat");

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Passes if the creation of a checksum does not fail.
	 * @throws IOException
	 */
	@Test
	public void testCheckSumCreation() throws IOException {
		SrlUpdateList list = createListFromFile(DEFAULT_DRAWING);
		SrlChecksum sum = Checksum.computeChecksum(list.getListList());
	}

	/**
	 * Passes if the creation of a checksum does not fail.
	 * @throws IOException
	 */
	@Test
	public void testCheckSumIsUnique() throws IOException {
		SrlUpdateList defaultList = createListFromFile(DEFAULT_DRAWING);

		SrlChecksum defaultSum = Checksum.computeChecksum(defaultList.getListList());
		SrlChecksum alteredSum = Checksum.computeChecksum(createAlteredList(defaultList).getListList());
		Assert.assertNotEquals(defaultSum, alteredSum);
	}

	/**
	 * Passes if the checksum list creates the same result as the comput checksum for each partial list
	 * @throws IOException
	 */
	@Test
	public void testCheckSumIsAdditive() throws IOException {
		SrlUpdateList defaultList = createListFromFile(DEFAULT_DRAWING);
		List<SrlUpdate> completeList = defaultList.getListList();

		List<SrlChecksum> partialSums = Checksum.computeListedChecksum(completeList);
		for (int i = 1; i <= completeList.size(); i++) {
			List<SrlUpdate> partialList = completeList.subList(0, i);
			System.out.println(partialList.size());
			SrlChecksum completeSum = Checksum.computeChecksum(partialList);
			System.out.println("Asserting for index " + i);
			System.out.println(completeSum.toString());
			System.out.println(completeSum.toString());
			Assert.assertEquals(partialSums.get(i - 1), completeSum);
		}
	}

	/*
	 * HELPER METHODS 
	 **/
	public static SrlUpdateList createListFromFile(final File f) throws IOException {
		return SrlUpdateList.parseFrom(new FileInputStream(f));
	}

	/**
	 * Should create an alternate form of the SrlUpdateList with only a single Id changed
	 * @param list
	 * @return
	 */
	public SrlUpdateList createAlteredList(SrlUpdateList list) {
		SrlUpdate first = list.getListList().get(0);
		SrlUpdate altered = SrlUpdate.newBuilder(first).setUpdateId("NEWID").build();
		SrlUpdateList.Builder newList = SrlUpdateList.newBuilder();
		newList.addList(altered);
		newList.addAllList(list.getListList().subList(1, list.getListList().size()));
		return newList.build();
	}
}
