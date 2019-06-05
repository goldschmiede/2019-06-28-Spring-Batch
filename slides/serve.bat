@echo off
call java12.bat
title reveal-js server
cd C:\Code\git\anderscore\revealjs-server
mvn spring-boot:run -Dspring-boot.run.arguments=--revealjs-server.project-base-dir=C:\Code\git\anderscore/goldschmiede-spring-batch/slides
