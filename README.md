# Alfred Home Server
**_UNDER CONSTRUCTION_**

## Summary
Project Alfred is a home monitoring server intended to be run on the Raspberry Pi.
It is designed to be flexible and allow users to add various input/output devices, 
such as doorbells, garage doors, electrical outlets, ceiling fans, lights, etc, 
throughout the home to be monitored and/or controlled by Alfred.

## Architect Overview

### Hardware
Devices sensors are connected to the Pi using conventional methods. If you're unsure
of how to connect input/output devices to the Pi please seek additional assistance.

**_Adding electrical devices requires domain knowledge and experience. Please proceed
with caution and at your own risk_**

### Software

#### Summary

Project Alfred implements a repository pattern, where the server and all clients maintain a synchronous state of all connected devices. The application implements this pattern by maintaining a run time data model on the server and on each client, and clients / servers keep each other in sync through messages. The nominal operational flow is as follows:

*On the Server:*
1. Input is detected on the Pi
2. Device is updated in Data Model
3. Message is sent to each client containing updated device date


*On the client:*
1. Message is received from the server
2. Device is updated in Data Model
3. Client does something with the new data model (updates display, etc)

#### Usage

Each hardware device connected to the Pi should have a corresponding plugin on the server. The plugin contains a NetworkHandler, which handles incoming messages, and a StateDeviceHandler, which handles the behavior for the device's states. 

##### NetworkHandler
The static Server class maintains a list of registered network handlers. When the server receives a message it notifies all registered handlers with an instance of the message. 

When you write a NetworkHandler, the only thing that really needs to be done is to convert the incoming message into a StateDevice, and update the StateDeviceManager. You can define other behaviors if your application desires, but it's not required.

After creating the handler register the handler with the static Server class so that you'll be notified when a message is received.

##### StateDeviceHandler
The static StateDeviceManager class maintains a list of registered StateDeviceHandlers. StateDeviceHandlers are tied to a specific device. When a device is updated the StateDeviceManager notifies all registered handlers with an instance of the updated device. 

When you write a StateDeviceHandler you get to define all the behaviors for a device's state. For example - a light: when the state is set to ON, tell the Raspberry pi to turn the light on. When the state is set to OFF, tell the Raspberry Pi to turn the light off.

After creating the handler register the handler with the StateDeviceManager so that you'll be notified when the device is updated. See java docs for usage syntax.
