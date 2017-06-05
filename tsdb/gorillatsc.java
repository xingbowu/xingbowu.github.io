

        <dependency>
            <groupId>fi.iki.yak</groupId>
            <artifactId>compression-gorilla</artifactId>
            <version>2.0.1</version>
        </dependency>


    private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesStream.class);
    private GorillaCompressor gorillaCompressor;
    LongArrayOutput output = new LongArrayOutput(3600);

    TimeSeriesStream(Long baseTime){
        long now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS)
                .toInstant(ZoneOffset.UTC).toEpochMilli();

        gorillaCompressor  = new GorillaCompressor(baseTime, output);
    }

    public void append(long timestamp, long value) throws IOException {
        gorillaCompressor.addValue(timestamp, value);
        gorillaCompressor.close();
    }


    public Map<Long, Long> readStream(long startTime, long endTime) {

        LongArrayInput input = new LongArrayInput(output.getLongArray());
        GorillaDecompressor gorillaDecompressor = new GorillaDecompressor(input);
        Pair pair;
        Map<Long, Long> dataPoint = new HashMap<>();
        while(true){
            pair = gorillaDecompressor.readPair();
            if(pair!=null){
                long timestamp = pair.getTimestamp();
                LOG.warn("TimeStamp: " + timestamp + " Value: " + pair.getLongValue());
                if(timestamp>=startTime/1000 && timestamp<=endTime/1000) {
                    dataPoint.put(pair.getTimestamp(), pair.getLongValue());
                }
            }else {
                break;
            }
        }
        return dataPoint;
    }
