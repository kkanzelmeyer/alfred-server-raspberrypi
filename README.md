# Alfred Home Server
**_UNDER CONSTRUCTION_**

## Summary
Project Alfred is a home monitoring server intended to be run on the Raspberry Pi.
It is designed to be flexible and allow users to add various input/output devices, 
such as doorbells, garage doors, electrical outlets, ceiling fans, lights, etc, 
throughout the home to be monitored and/or controlled by Alfred.

__WARNING - Connecting high voltage electrical devices to the Raspberry Pi is no joke. You need to know what you're doing. There are plenty of good tutorials (and even more bad tutorials) on the internet showing how to connect devices. Please proceed with caution and at your own risk__

## Getting Started
#### Install Java 8 (min)

I'm told that Raspbian 8 includes Java 8 since early 2015. If you're running Raspbian ssh into your Pi and type:
```
sudo apt-get upgrade
sudo apt-get update
```

Then check your Java version by running

`java -version`


If you still do not have Java 8, try running

`sudo apt-get install oracle-java8-jdk`

#### Install Alfred

1. __Clone__ This repository in your IDE (Eclipse, IntelliJ, etc)
2. __Build__ the project using Maven; ie. `mvn install`
3. __Deploy__ the jar to your Raspberry Pi using secure copy.
      - in a terminal navigate to [project root]/target and find the jar with dependencis there
      - Secure copy to your Pi using your username and IP Address. 
        For example:
       `scp Alfred-server-with-dependencies.jar pi@192.168.1.100:~/` 
        This will place the jar in your home directory
4. __Configure__ Once the jar has been copied the last step is to create a few files and directories. SSH into your Pi. First lets create a directory for your Alfred project root
   
   `cd ~` 
   `sudo mkdir Alfred`
   
   Inside the Alfred directory make two more directories:
   
   `sudo mkdir img` 
   `sudo mkdir cfg`
   
   Inside the "cfg" directory create two files:
   
   `sudo touch config.properties` 
   `sudo touch devices.json`
   
   See the `cfg` directory for this repository for instructions for the properties file as well as example files.
5. __Start__ Alfred. In the terminal run `sudo java -cp [path.to.server.app]...`

If you see the Welcome message displayed by the server then congratulations! You're ready to start adding hardware!
