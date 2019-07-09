package org.hazelcast.iotdemo;


import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
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
                    SAMPLE_DATA_CSV_FILE_PATH, true, 1))
                    .withNativeTimestamps(5000)
                    .filter(DataPoint::isValid);
        } catch (IOException e) {
            System.err.println("could not read data from input file " +
                    SAMPLE_DATA_CSV_FILE_PATH);
            System.exit(127);
        }

        // We fork the stream into two forks, the violation detection fork,
        // which detects speed violations and stores the number of violations
        // per vehicle in an IMap, and the geolocation fork, which passes
        // all valid data points through whether they represent speed
        // violations or not such that all vehicle movements are reflected
        // on the map widget in the web dashboard
        StreamStage<DataPoint> violationDetectionFork = sourceStage.filter(
                DataPoint::isPolicyViolation);
        StreamStage<DataPoint> geolocationFork = sourceStage;

        // Drain out the violations fork -- this goes to a file now, in the
        // future, it will go to an IMap
        violationDetectionFork
                .groupingKey(DataPoint::getDriverId)
                .rollingAggregate(AggregateOperations.counting( ))
                .drainTo(Sinks.files("violations-out"));


        // Drain out the geolocation fork -- this goes to a file now, in the
        // future, it will go into a queue
        geolocationFork.drainTo(Sinks.files("coordinates-out"));

        JetInstance jet = Jet.newJetInstance( );
        try {
            jet.newJob(p).join( );
        } finally {
            jet.shutdown( );
        }
    }
}
