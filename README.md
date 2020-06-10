# Fuzzy-Logic-Pump-Controller
Instructions to compile and run the code :

Setup Java environment in linux by running following commands in terminal.
a. $ sudo apt install default - jre
b. $ sudo apt install default - jdk
	

Put all the .jar files and .java file in the same folder.

In terminal cd to the folder containing jar files and code file.

Run the following command in terminal to compile :
        javac -classpath .:jfuzzylite.jar:core.jar FuzzyPumpController.java
        java -classpath .:jfuzzylite.jar:core.jar FuzzyPumpController
