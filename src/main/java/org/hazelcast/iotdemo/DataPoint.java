package org.hazelcast.iotdemo;


import com.sun.istack.internal.NotNull;

import java.io.Serializable;


public final class DataPoint implements Serializable
{
    private static final double POLICY_VIOLATION_THRESHOLD_MPH = 45.0;

    private int driverId;
    private long messageTime;
    private boolean valid;
    private double speed;
    private boolean policyViolation;
    private double longitude;
    private double latitude;

    private static double metersPerSecondToMph(final double mps)
    {
        return mps * 2.237;
    }

    public DataPoint(@NotNull String csvRow)
    {
        String[ ] features = csvRow.split(",");

        try {
            this.driverId = Integer.parseInt(features[1]);
            this.messageTime = (long) (Double.parseDouble(features[2]));
            // Speed in the raw data is measured in meters per second, but
            // policy violations are detected in the more idiomatic (at least
            // in the USA) miles per hour, so convert.
            this.speed = metersPerSecondToMph(features[8].isEmpty( ) ? 0.0 :
                    Double.parseDouble(features[8]));
            this.latitude = Double.parseDouble(features[5]);
            this.longitude = Double.parseDouble(features[6]);

            valid = true;

            policyViolation = speed > POLICY_VIOLATION_THRESHOLD_MPH;
        } catch (Throwable t) {
            // Catch a NumberFormatException or any other unchecked exception
            // which might be thrown during data parsing (the data isn't
            // guaranteed to be clean). If such an exception is generated,
            // mark this DataPoint invalid. Invalid DataPoint instances
            // will be filtered out.
            valid = false;
        }
    }

    public boolean isValid( )
    {
        return valid;
    }

    public long getMessageTime( )
    {
        return messageTime;
    }

    public boolean isPolicyViolation( )
    {
        return policyViolation;
    }

    public int getDriverId( )
    {
        return driverId;
    }

    public double getSpeed( )
    {
        return speed;
    }

    public double getLongitude( )
    {
        return longitude;
    }

    public double getLatitude( )
    {
        return latitude;
    }

    @Override
    public String toString( )
    {
        return "DataPoint{" +
                "driverId=" + driverId +
                ", messageTime=" + messageTime +
                ", valid=" + valid +
                ", speed=" + speed +
                ", policyViolation=" + policyViolation +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
