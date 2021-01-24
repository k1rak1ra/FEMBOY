# F.E.M.B.O.Y.
Finds Every Match Beyond Ordinary Yearning  
Because it finds all the tags, not just the ones you're looking for  
A comprehensive image management, tagging, and syncing system, all automated  

# Project Overview
Using DeepDanbooru:
https://github.com/KichangKim/DeepDanbooru

Mobile clients: (iOS, Android)
- Most likely made using Flutter
- Runs either in local mode or in server client mode
- In local mode, DD instance is run locally and all images and tag data are saved locally
- In server client mode, DD instance is run on server and client can either have no images saved locally, only certain tags, only certain images, or all images
- For local mode, downloads DD model from DD master server
- Can change DD master server to self-hosted one
- Can edit/delete tags of images
- Can upload/delete images
- In server client mode, upload image to server and server returns tags

Desktop clients: (Windows, Linux, MacOS, BSD?)
- Most likely made using Revery
- Runs either in local mode or in server client mode
- In local mode, DD instance is run locally and all images and tag data are saved locally
- In server client mode, DD instance is run on server and client can either have no images saved locally, only certain tags, only certain images, or all images
- For local mode, downloads DD model from DD master server
- Can change DD master server to self-hosted one
- Can edit/delete tags of images
- Can upload/delete images
- In server client mode, upload image to server and server returns tags

Server: 
- Most likely made in Java + mySQL using Java self-contained webserver
- Runs DD instance 
- Downloads DD model from DD master server
- Can change DD master server to self-hosted one
- Has user accounts that clients need to use to login
- Can pull user account from LDAP and can assign LDAP group to server admin group
- Has admin panel site
- Serves images and tags

DD master server:
- Pulls new Danbooru data every x days
- Retrains model
- Serves retrained model

# Milestones
Phase 1:
    Desktop client running in local mode, no DD master server - uses manually trained model data.
