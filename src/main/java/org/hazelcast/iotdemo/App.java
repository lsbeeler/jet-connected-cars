package org.hazelcast.iotdemo;


import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.pipeline.*;

import java.io.IOException;

public class App
{
    private static final String SAMPLE_DATA_CSV_FILE_PATH =
            "data/AMCP-Probe-Data.csv";

    public static void main(String[ ] args)
    {
        Pipeline p = Pipeline.create( );

        StreamStage<DataPoint> sourceStage = null;
        try {
            sourceStage = p.drawFrom(AMCPDataSource.createSource(
                    SAMPLE_DATA_CSV_FILE_PATH, true, 500))
                    .withNativeTimestamps(10_000)
                    .filter(DataPoint::isValid);
        } catch (IOException e) {
            System.err.println("could not read data from input file " +
                    SAMPLE_DATA_CSV_FILE_PATH);
            System.exit(127);
        }

        sourceStage.drainTo(Sinks.logger( ));

        JetInstance jet = Jet.newJetInstance( );
        try {
            jet.newJob(p).join( );
        } finally {
            jet.shutdown( );
        }
    }
}
