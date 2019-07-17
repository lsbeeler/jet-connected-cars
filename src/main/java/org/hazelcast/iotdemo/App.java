package org.hazelcast.iotdemo;


import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;

import java.io.IOException;


public class App
{
    private static final String SAMPLE_DATA_CSV_FILE_PATH =
            "data/AMCP-Probe-Data.csv";
    private static final String VIOLATIONS_MAP = "violations-map";
    private static final String COORDINATES_MAP = "coords-map";

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
        StreamStage<DataPoint> violationDetectionFork = sourceStage;
        StreamStage<DataPoint> geolocationFork = sourceStage;

        // Handle the violations detection fork. This is the more complex of the
        // the two.
        violationDetectionFork
                .map(DataPointPolicyWrapper::new)
                .filter(DataPointPolicyWrapper::isPolicyViolation)
                .groupingKey(wrapper -> wrapper.getDataPoint( ).getDriverId( ))
                .rollingAggregate(AggregateOperations.counting( ))
                .drainTo(Sinks.map(VIOLATIONS_MAP));

        // Handle the geolocation fork
        geolocationFork
                .map(pt -> new GeolocationEntry(pt.getDriverId( ),
                        new GeolocationCoordinates(pt)))
                .drainTo(Sinks.map(COORDINATES_MAP));

        JetInstance jet = Jet.newJetInstance( );

        MapDumper.start(
                jet.getHazelcastInstance( ).getMap(COORDINATES_MAP));
        try {
            jet.newJob(p).join( );
        } finally {
            jet.shutdown( );
        }
    }
}
