VitaReminder
-----

VitaReminder is a desktop application written in Java that lets you 
keep track of vitamins, supplements and medicines. You can schedule reminders 
via e-mail, text message or automated voice messages. 

It was developed under Windows 7 64-bit using Eclipse Kepler and JDK 1.7.

It has been tested on the following platforms with Java 1.7 installed:

  - Windows 7 (64-bit)
  - Windows 8 (64-bit)
  - Mac OS X (10.7.5)
  - Linux (Fedora 20)

<img src="https://raw.githubusercontent.com/jjobes/VitaReminder/master/screenshots/1.png?token=AClj440G0BctY1bS0w1vENeGX7gAWaMVks5U3XoswA%3D%3D" width="500">

Requirements
------------
Java 1.7 or higher

Build Instructions
------------------
Open up a command line and enter:
```sh
git clone https://github.com/jjobes/VitaReminder.git
cd VitaReminder
ant
```
This will create a 'dist' directory. You will find an executable .jar file there.

Import the project into Eclipse
-------------------------------
Alternatively, you can open up the project in Eclipse.

1. First clone the repository to a local directory using Git.
2. In Eclipse, choose File -> Import -> General -> Existing Projects into Workspace -> Browse -> then select the directory that you downloaded.
3. Press Finish.
4. Please ensure that Eclipse is configured to use at least Java 1.7 as its runtime JRE. You can check this by choosing the following Eclipse menus: Run -> Run Configurations... -> choose the JRE tab in the right pane.  Here you can see which JRE / JDK Eclipse is using for this project's execution environment.
5. Press the Run button (or Ctrl+F11) to have Eclipse build and run the application.

Environment Variables
---------------------
This application uses the [Tropo](https://www.tropo.com/) API to send text messages and automated voice messages to your phone. You can sign up for a free Tropo developer account and then use the authentication tokens you are given to set the following environment variables on your local machine:

```
TROPO_TOKEN_TEXT_MESSAGE
```

```
TROPO_TOKEN_VOICE_MESSAGE
```

These are referenced by ```TextMessage.java``` and ```VoiceMessage.java``` in ```com.vitareminder.reminders```. Read the comments for the ```send()``` method in each of those files for the scripts that you should use on Tropo's server.

You must also set the following two environment variables on your local machine:

```
VITAREMINDER_EMAIL_NAME
```

```
VITAREMINDER_EMAIL_PASSWORD
```

Your e-mail reminders will be sent from this e-mail account. Note that ```VITAREMINDER_EMAIL_NAME``` should be your Gmail username. For example, if your Gmail address is ```joesmith@gmail.com```, you should set ```VITAREMINDER_EMAIL_NAME``` to ```joesmith```. These two environment variables are retrieved in ```ReminderManager.java``` in ```com.vitareminder.reminders```.