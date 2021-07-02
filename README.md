# Tempo
Tempo is a music bot for PS4 and PS5, it is similar to many music bots used on Discord servers. 

Prerequisites:
  - Jar files, included in Jar folder
  - Eclipse or any other Java IDE
  - VB-CABLE Virtual Audio Device, https://vb-audio.com/Cable/
  - Two Playstation consoles
  - Two Paystation accounts
  - Playstation Remote Play, https://remoteplay.dl.playstation.net/remoteplay/lang/en/index.html
  - New gmail account with allow less secure apps on

One Time Setup:
  - Create a new gmail account
  - Go to https://myaccount.google.com/lesssecureapps, and allow less secure apps
  - Git clone this repository, https://github.com/Andre-Ceschia/Tempo
  - Open eclipse and create a new project
  - Uncheck "Use default location", click browse
  - Navigate to the folder you have just cloned and click "Select Folder"
  - Click "Next" then "Finish"
  - Replace the values of tempoFolderLocation, email and pass with thier respective values

Playstation Setup:
  - With the Playstation that you are not playing on create a party
  - Connect via remote play to the afformentioned playstation
  - Make sure the microphone is on in remote play
  - In the Windows start menu look up "Change system sounds"
  - In recording and playback disable your microphone and speaker and enable the virtual cable output and input
  - Join the playstation party with the other playstation
  - You or any of your friends can now start sending the bot commands via email

[![Setup Tutorial Video Here](https://img.youtube.com/vi/gCoqLMMf9dA/0.jpg)](https://www.youtube.com/watch?v=gCoqLMMf9dA)

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Video that outlines all steps of the setup.

How To Use:
  - To send a command to the bot the command must be in the subject of the email
  - If the "help" command is sent to the bot it will reply with a help menu
  - If a youtube video link is sent to the bot, it will start playing the audio of said youtube video or add it to the queue if audio is already being played
  - If the "pause" command is sent to the bot while audio is being played the bot will pause the audio
  - If the "play" command is sent to the bot while audio is paused it will resume the audio being played
  - If the "skip" command is sent to the bot while audio is being played or paused the next song in the queue will start being played
