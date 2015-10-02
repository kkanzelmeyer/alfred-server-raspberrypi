# Alfred Home Server


## Summary
Project Alfred is a home monitoring server intended to be run on the Raspberry pi.
It is designed to be flexible and allow users to add various input/output devices, 
such as doorbells, garage doors, electrical outlets, ceiling fans, lights, etc, 
throughout the home to be monitored and/or controlled by Alfred

**_Adding electrical devices requires domain knowledge and experience. Please proceed
with caution and at your own risk_**

## Architect Overview

### Hardware
Devices sensors are connected to the Pi using conventional methods. If you're unsure
of how to connect input/output devices to the Pi please seek additional assistance.

### Software

#### Summary

Project Alfred implements a client / server synchronous data model, where the server
and all clients maintain a synchronous state of all connected devices. It does this by
maintaining a run time data model on the server and on each client. The nominal 
operational structure is as follows:

*On the Server:*

1. Input is detected on the Pi
2. Device is updated in Data Model
3. Message is sent to each client containing updated device date


*On the client:*

1. Message is received from the server
2. Device is updated in Data Model
3. Client does something with the new data model (updates display, etc)

#### Usage

Each hardware device connected to the Pi should have a corresponding plugin and state handler
implemented on the server.

