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