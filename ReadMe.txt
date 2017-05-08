Idea:-
------
https://github.com/WomenWhoCode/KL-network/wiki/Project-Miki

This is an open project to create an Android application to allow user the ability to split bill by taking a photo of the receipt  after those gatherings with friends and family in a fun and simple way! 

Features:-
1. Using phone's camera to capture Receipts
2. Import photos from photo gallery
3. Read the items and amount from the receipt
4. Allow to select which item want to pay for.
5. Calculate the receipt ,3 methods available :
	  i)  Split the total amount evenly to everyone
	  ii) Select each item and it's amount by person
	  ii) Select which item to split evenly + each item and amount by person

Android version supported - Android 4.1 Jelly Bean and above

Project Structure:- 

├─ external libraries
├─ app
│  ├─ libs
│  ├─ src
│  │  ├─ androidTest
│  │  │  └─ java
│  │  │     └─ wwckl/projectmiki
│  │  └─ main
│  │     ├─ java
│  │     │  └─ wwckl/projectmiki
│  │     ├─ res
│  │     └─ AndroidManifest.xml
│  └─ proguard-rules.pro
├─ build.gradle
└─ settings.gradle

Java Packages Architecture:-

wwckl/projectmiki
├─ models
├─ utils
├─ fragments
└─ views
   ├─ adapters
   ├─ actionbar
   ├─ widgets
   └─ notifications

High-level Technical Req:-
--------------------------
1. OCR - https://code.google.com/p/tesseract-ocr/
2. Android Resources: - https://source.android.com/, http://developer.android.com/training/basics/firstapp/index.html
 
Project Management Tools - https://trello.com/b/eTg0MwUT/project-miki

Reference:-
https://github.com/futurice/android-best-practices
