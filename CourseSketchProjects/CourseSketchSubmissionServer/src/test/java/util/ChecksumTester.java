package util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.submission.Submission.SrlChecksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ChecksumTester {
	final File DEFAULT_DRAWING = new File("CourseSketchProjects/CourseSketchSubmissionServer/src/resources/tst/testDrawing.dat");

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Passes if the creation of a checksum does not fail.
	 *
	 * @throws IOException
	 */
	@Test
	public void testCheckSumCreation() throws IOException {
		SrlUpdateList list = createListFromFile(DEFAULT_DRAWING);
		SrlChecksum sum = Checksum.computeChecksum(list.getListList());
	}

	/**
	 * Passes if the creation of a checksum does not fail.
	 *
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
	 * Passes if the checksum list creates the same result as the comput
	 * checksum for each partial list
	 *
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
			Assert.assertEquals(partialSums.get(i - 1), completeSum);
		}
	}

	/**
	 * Passes if the checksum list creates the same result as the comput
	 * checksum for each partial list
	 *
	 * @throws IOException
	 */
	@Test
	public void testCheckSumListIsComplete() throws IOException {
		SrlUpdateList defaultList = createListFromFile(DEFAULT_DRAWING);
		List<SrlUpdate> completeList = defaultList.getListList();

		List<SrlChecksum> partialSums = Checksum.computeListedChecksum(completeList);
		SrlChecksum defaultSum = Checksum.computeChecksum(completeList);
		Assert.assertEquals(defaultSum, partialSums.get(partialSums.size() - 1));
	}

	/**
	 * Passes if the checksum list creates the same index as the list in its
	 * proper location
	 *
	 * @throws IOException
	 */
	@Test
	public void testCheckSumIndexGrabberReturnsCorrectIndex() throws IOException {
		SrlUpdateList defaultList = createListFromFile(DEFAULT_DRAWING);
		List<SrlUpdate> completeList = defaultList.getListList();
		final int halfIndex = completeList.size() / 2;
		SrlChecksum halfSum = Checksum.computeChecksum(completeList.subList(0, halfIndex + 1));

		final int resultIndex = Checksum.checksumIndex(completeList, halfSum);
		Assert.assertEquals(halfIndex, resultIndex);
	}

	/**
	 * Passes if the checksumIndex function returns -1 for an index that does not exist
	 *
	 * @throws IOException
	 */
	@Test
	public void testCheckSumIndexGrabberReturnsNegativeOne() throws IOException {
		SrlUpdateList defaultList = createListFromFile(DEFAULT_DRAWING);
		List<SrlUpdate> completeList = defaultList.getListList();

		final int resultIndex = Checksum.checksumIndex(completeList, SrlChecksum.newBuilder().setFirstBits(0).setSecondBits(0).build());
		Assert.assertEquals(-1, resultIndex);
	}

	/*
	 * HELPER METHODS
	 */
	public static SrlUpdateList createListFromFile(final File f) throws IOException {
		System.out.println(f.getAbsolutePath());
		return SrlUpdateList.parseFrom(new FileInputStream(f));
	}

	/**
	 * Should create an alternate form of the SrlUpdateList with only a single
	 * Id changed
	 *
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
