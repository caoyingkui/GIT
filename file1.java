public class temp {public void SimpleTextBKDWriter(int maxDoc, Directory tempDir, String tempFileNamePrefix, int numDataDims, int numIndexDims, int bytesPerDim,
                                              int maxPointsInLeafNode, double maxMBSortInHeap, long totalPointCount, boolean singleValuePerDoc) throws IOException {
    verifyParams(numDims, maxPointsInLeafNode, maxMBSortInHeap, totalPointCount);
    // We use tracking dir to deal with removing files on exception, so each place that
    // creates temp files doesn't need crazy try/finally/sucess logic:
    this.tempDir = new TrackingDirectoryWrapper(tempDir);
    this.tempFileNamePrefix = tempFileNamePrefix;
    this.maxPointsInLeafNode = maxPointsInLeafNode;
    this.numDims = numDims;
    this.bytesPerDim = bytesPerDim;
    this.totalPointCount = totalPointCount;
    this.maxDoc = maxDoc;
    this.offlineSorterBufferMB = OfflineSorter.BufferSize.megabytes(offlineSorterBufferMB);
    this.offlineSorterMaxTempFiles = offlineSorterMaxTempFiles;
    docsSeen = new FixedBitSet(maxDoc);
    packedBytesLength = numDims * bytesPerDim;

    scratchDiff = new byte[bytesPerDim];
    scratch1 = new byte[packedBytesLength];
    scratch2 = new byte[packedBytesLength];
    commonPrefixLengths = new int[numDims];

    minPackedValue = new byte[packedBytesLength];
    maxPackedValue = new byte[packedBytesLength];

    // If we may have more than 1+Integer.MAX_VALUE values, then we must encode ords with long (8 bytes), else we can use int (4 bytes).
    this.longOrds = longOrds;

    this.singleValuePerDoc = singleValuePerDoc;

    // dimensional values (numDims * bytesPerDim) + ord (int or long) + docID (int)
    if (singleValuePerDoc) {
        // Lucene only supports up to 2.1 docs, so we better not need longOrds in this case:
        assert longOrds == false;
        bytesPerDoc = packedBytesLength + Integer.BYTES;
    } else if (longOrds) {
        bytesPerDoc = packedBytesLength + Long.BYTES + Integer.BYTES;
    } else {
        bytesPerDoc = packedBytesLength + Integer.BYTES + Integer.BYTES;
    }

    // As we recurse, we compute temporary partitions of the data, halving the
    // number of points at each recursion.  Once there are few enough points,
    // we can switch to sorting in heap instead of offline (on disk).  At any
    // time in the recursion, we hold the number of points at that level, plus
    // all recursive halves (i.e. 16 + 8 + 4 + 2) so the memory usage is 2X
    // what that level would consume, so we multiply by 0.5 to convert from
    // bytes to points here.  Each dimension has its own sorted partition, so
    // we must divide by numDims as wel.

    maxPointsSortInHeap = (int) (0.5 * (maxMBSortInHeap * 1024 * 1024) / (bytesPerDoc * numDims));

    // Finally, we must be able to hold at least the leaf node in heap during build:
    if (maxPointsSortInHeap < maxPointsInLeafNode) {
        throw new IllegalArgumentException("maxMBSortInHeap=" + maxMBSortInHeap + " only allows for maxPointsSortInHeap=" + maxPointsSortInHeap + ", but this is less than maxPointsInLeafNode=" + maxPointsInLeafNode + "; either increase maxMBSortInHeap or decrease maxPointsInLeafNode");
    }

    // We write first maxPointsSortInHeap in heap, then cutover to offline for additional points:
    heapPointWriter = new HeapPointWriter(16, maxPointsSortInHeap, packedBytesLength, longOrds, singleValuePerDoc);

    this.maxMBSortInHeap = maxMBSortInHeap;
}}