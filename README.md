# OPSC7312-BirdWatch
Bird Watching App for OPSC7312 
OPSC Part 2
=======================================
st10082120, st10082256, st10082240
Daniel Heilbron, Daniel Jacobs, Matthew Bosch

NB: Please Read all Instructions

Installation
------------
To install, follow these steps:

1. Open in Android Studio:
   - Launch Android Studio.
   - Choose "Open an existing Android Studio project" from the welcome screen.
   - Navigate to the directory where you cloned the repository and select it.
   - Click "OK" to open the project.

2.  Build and Run:
   - Connect an Android device to your computer or use an emulator.
   - NB: Sync the Gradle files
   - if an error relating to the jdk occurs
	ensure the jdk setting suggested by the build is set to default 
   - In Android Studio, click the "Run" button (a green triangle) in the toolbar or press Shift + F10.
   - Select your device or emulator from the list, and Android Studio will build and install the app.
   - The app should automatically start on the selected device or emulator.
   - NB: in the case that the project doesnt build(sdk error only)
   - 
     Method 1(Recommended):
	-Open the local.properties file: 
		This file is usually located in the root directory of your Android Studio project.

	- Locate the sdk.dir line: 
		This line specifies the location of your Android SDK.

	- Replace the existing path after sdk.dir= with the path to your Android SDK directory. 
	- The path should be an absolute path and it should use double backslashes (\\) as separators. 
	- For example, 
		if the Android SDK is located at C:\Users\JohnDoe\AppData\Local\Android\Sdk, 
		you should update the line to look like this: sdk.dir=C:\\Users\\JohnDoe\\AppData\\Local\\Android\\Sdk.
  	- sync gradle (again if already done) and continue
   
Method 2:
 	- add ANDROID_HOME variable in "Environment Variables' settings
	- C:\Users\USER\AppData\Local\Android\Sdk ,this is generally where sdk files are stored
	- paste the location of your sdk file in the 'value' column and press 'OK'



   - In Android Studio, click the "Run" button (a green triangle) in the toolbar or press Shift + F10.
   - Select your device or emulator from the list, and Android Studio will build and install the app.
   - The app should automatically start on the selected device or emulator.

