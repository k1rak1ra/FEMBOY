# F.E.M.B.O.Y.
Finds Every Match Beyond Ordinary Yearning  
Because it finds all the tags, not just the ones you're looking for  
A comprehensive image management, tagging, and syncing system, all automated  

# What is it?
Do you have a huge folder of anime images, with no hope of ever finding anything in it? Do you want to be able to access this folder across machines and on your phone?
If so, you've come to the right place!
With auto-tagging powered by DeepDanbooru, image organization bliss is one batch upload away.
If you have multiple computers you use + a phone and want to access your image library everywhere, or are just a homelabber, the server is for you.

# Project Overview
Using DeepDanbooru:
https://github.com/KichangKim/DeepDanbooru

Inspired by Hydrus (https://github.com/hydrusnetwork/hydrus), and intending to be a nicer-looking and easier to use version of it with far better server/sync features and a modern neural network based automated tagging system.

Mobile client (INCOMPLETE): (iOS, Android) 
- Most likely made using Flutter
- Runs either in local mode or in server client mode
- In local mode, DD instance is run locally and all images and tag data are saved locally
- In server client mode, DD instance is run on server and client can select some images to be saved locally
- Can edit/delete tags of images
- Can upload/delete images
- In server client mode, upload image to server and server returns tags

Desktop client (Version 0.1.0 available): (Windows, Linux, MacOS, BSD?)
- Requires Java 11+ for Linux or ONLY Java 1.8 for Windows and JAR
- Runs either in local mode or in server client mode
- In local mode, DD instance is run locally and all images and tag data are saved locally
- In server client mode, DD instance is run on server and client can select some images to be saved locally
- In server client mode, the server performs the computationally/RAM-intensive TensorFlow image tagging operations

Server (Version 0.1.0 available): 
- Requires Java 11+ on all platforms
- Requires a mySQL server
- Has user accounts that clients need to use to login
- LDAP support
- Server API documentation coming soon so you can make your own client
- HTTP on port 5000. Designed to run on LAN or behind a reverse proxy

# Contributing
- This is a first release and probably isn't perfect. If you have a problem, submit an issue. Be sure to include the error message (if any)
- I use Fedora Linux and don't do extensive testing on Windows beyond providing exe binaries and running some quick tests, and do not provide or test on a Mac at all (although the jar should still work just fine) Issue/incompatability reports from users of Windows, Mac, or other distros are appreciated
- Contributors/maintainers on other platforms would be appreciated
- If there's a feature you want implemented, feel free to submit a pull request or an Enhancement issue

# Issue reporting template
1. What were you doing?
2. What happened?
3. What should've happened?
4. Stack trace/error message

# Future milestones
- Mobile app creation
- End reliance on the provided DeepDanbooru model, train own based on newer dataset. Automate training and model updating

# Development environment
For server/desktop client: ItelliJ IDEA + Maven for dependencies
Place DD-model.zip in DD_data folder of client and server, alongside the DD-characters.txt and DD-tags.txt that are already there. The model file is too big for git.