public class temp {public void SimpleTextBKDWriter(int maxDoc, Directory tempDir, String tempFileNamePrefix, int numDataDims, int numIndexDims, int bytesPerDim,
                                              int maxPointsInLeafNode, double maxMBSortInHeap, long totalPointCount, boolean singleValuePerDoc) throws IOException {
    this(maxDoc, tempDir, tempFileNamePrefix, numDataDims, numIndexDims, bytesPerDim, maxPointsInLeafNode, maxMBSortInHeap, totalPointCount, singleValuePerDoc,
            totalPointCount > Integer.MAX_VALUE, Math.max(1, (long) maxMBSortInHeap), OfflineSorter.MAX_TEMPFILES);
}}