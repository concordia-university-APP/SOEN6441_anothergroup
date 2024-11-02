# SOEN6441_anothergroup

#Configuration
Clone the repo in the folder location you want to work with
open a terminal in this location and run the following commands:
- sbt clean
- sbt compile
- sbt eclipse

Now, you can import the project into eclipse:
1. import
2. existing projects into workspace
3. select the root folder of the project

Good, you have now installed the project.

To run the app, you have to run the command: sbt run
I recommend you make an external tool with the following parameters
- Location: C:\WINDOWS\system32\cmd.exe
- Working Directory: ${workspace_loc:/project}
- Arguments: /c start http://localhost:9000 & sbt run

This will both open a web page and run the app

To run the project on IntelliJ
 - clone the project from github
 - install scala and play routes 2 plugin
 - from project setting set project sdk to jdk 11 or jdk 8
 - edit file configuration and add playApp as envrionment
 - from services, add play app as service
 - use the sbt shell (can be found by ctlr+shift+s) and type clean first
 - type compile on sbt shell
 - if project compile successfully then the project is ready to run

For the youtube api
- open a google cloud account
- create a new project with your choice of name
- enable youtube data v3 api on that project
- set up a api key in google cloud account credential on that project
- in the applciation conf file set apiKey value to your google cloud acocunt credential
- set the applicationName value to your project name on google cloud account

run the project from service