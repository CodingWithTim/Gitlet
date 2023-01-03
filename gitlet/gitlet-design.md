# Gitlet Design Document
author: Tim Li

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

###Gitlet
* GITLET FOLDER - .gitlet folder directory path
* OBJECT FOLDER - object folder directory path, stores commit objects and blobs
###Reference
* FILE - the file path that contains the reference object
* CWD - current working directory
* Head - head commit
* Master - master branch
* Branches - an Arraylist that stores the heads of all branches
###Stage
* FILE - the file path that contains the Stage object
* Additions - A Hashmap of hash of files that need to be added when committing
* Removals - A HashMap of hash of files that need to be removed when committing
###Commit
* Message - contains the message of the commit
* Timestamp - time at which a commit was created. Assigned by constructor.
* Parent - the parent of the commit object. Assigned by constructor.
* Blobs - A HashMap of Blob
###Blob
* Hash - hashcode of the file
* File - location of the file
* Content - content of the file
## 2. Algorithms
Haven't started merge yet.

## 3. Persistence
Stores all the necessary information and pointers in the Reference object, which is serialized in a file. When need to use this information every time we run the program, we simply need to load the Reference file.
Commit folder stores all the commit file. Blob folder stores all the blob files.

## 4. Design Diagram



