# Hazelcast Jet Connected Car Demo

## Overview

The application combines the [Hazelcast Jet](https://jet.hazelcast.org) stream
processing engine with the [Hazelcast IMDG](https://hazelcast.org) in-memory
data grid to make business policy decisions based on a continuous stream of
data from connected vehicles.

## Data Set

The data set used is the AMCP (Advanced Messaging Concept Development) Probe
Vehicle data set. The AMCP project is a joint effort of the University of
Michigan and the United States Department of Transportation. The data set
contains data on over 30 features of each connected vehicle, including
geolocation, speed, tire air pressure, etc. A complete description of data set
features is available
[here](https://www.opendatanetwork.com/dataset/data.transportation.gov/7ee5-ppkq)
on the Open Data Network.

## Project Status: MVP

As of 30 June 2019, the demo has reached minimum viable product status: it is
capable of ingesting data from the complex AMCP CSV format, culling
data with incomplete sensor readings on critical features, and detecting
policy violations.

The detection of "policy violations" represents the key business logic of the
application and its value to business decisionmakers. All of the connected
vehicles in the data set are driven in a limited radius around the University
of Michigan campus in Ann Arbor, Michigan. Since the U-M campus is situated in
a suburban, mostly residential setting outside of Detroit, the maximum
permissible speeds on streets never exceeds 45 miles per hour. Each time a
driver exceeds 45 miles per hour, business rules engine deems this a "policy
violation."

Right now, the system is capable of detecting policy violations and printing
them to the console. In the next iteration of the project, targeted for
delivery on 31 July 2019, functionality will go much further.

## Work for the Next Iteration

Using the stateful processing features offered by the combination of Hazelcast
Jet and Hazelcast IMDG, we envision not just detecting speed policy violations
but counting them for each driver. Some speed policy violations may be
unavoidable: for instance, increased speed may be necessary to pass a stopped
or slow moving vehicle. However, if an individual driver accumulates several
policy violations during a the sampling window, this likely indicates a
reckless driver and the need to make a business policy decision to reprimand or
terminate the driver.

In its final form, we envision a web UI that includes a moving map widget as
well as a table widget that quantifies "driver risk" (i.e., the number of
speed policy violations). A mockup of this web UI is shown in the image below:

![Driver Risk Mockup](https://lsbeeler-exchange.s3-us-west-2.amazonaws.com/DriverRisk.png)