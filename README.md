TL;DR
=====

njord helps you when pentesting cloud platform by resigning requests after BURP alterations

Basics
======

njord is a burp plugin with only one dependency : Apache Common-Codecs (http://commons.apache.org/proper/commons-codec/)

Usage
=====

 - Modify njord.properties with your test credential and put in the directory from which you start burp
 - Modify your /etc/hosts and configure BURP in transparent proxy to your target
 - Load the extension jar file inside Extender tab
 - Make sure that Burp has access to common-codecs.jar from Apache (Lib path in Extender / Path)

Enjoy ;-) 
