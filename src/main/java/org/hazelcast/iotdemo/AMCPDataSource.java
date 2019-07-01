package org.hazelcast.iotdemo;


import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


public final class AMCPDataSource
{
    private static final int SOURCE_BUFFER_SIZE = 4;

    private static class AMCPParser
    {
        private final List<String> lines;
        private int pos;
        private long intervalTimeMsec;

        public AMCPParser(List<String> csvLines, long intervalTimeMsec)
        {
            this.lines = csvLines;
            this.pos = 0;
            this.intervalTimeMsec = intervalTimeMsec;
        }

        public void fillBuffer(SourceBuilder.TimestampedSourceBuffer<DataPoint>
                buffer)
        {
            for (int i = 0; i < SOURCE_BUFFER_SIZE; i++) {
                if (pos == lines.size( ))
                    return;

                DataPoint d = new DataPoint(lines.get(pos));
                pos++;

                if (d.isValid( ))
                    buffer.add(d, d.getMessageTime( ));
            }

            LockSupport.parkNanos(
                    TimeUnit.MILLISECONDS.toNanos(intervalTimeMsec));
        }
    }

    public static StreamSource<DataPoint> createSource(String csvPath,
            boolean csvHasHeaderRow, long intervalTimeMsec) throws IOException
    {
        BufferedReader csvReader = new BufferedReader(
                new FileReader(csvPath));

        ArrayList<String> lines = new ArrayList<>( );
        if (csvHasHeaderRow)
            csvReader.readLine( );
        String line = csvReader.readLine( );
        while (line != null) {
            lines.add(line);
            line = csvReader.readLine( );
        }

        return SourceBuilder.timestampedStream("amcp-data-source",
                context -> new AMCPParser(lines, intervalTimeMsec))
                .fillBufferFn(AMCPParser::fillBuffer)
                .build( );
    }
}
