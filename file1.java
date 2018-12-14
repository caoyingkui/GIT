public void doSplit() throws IOException {

        List<LeafReaderContext> leaves = searcher.getRawReader().leaves();
        Directory parentDirectory = searcher.getRawReader().directory();

        }