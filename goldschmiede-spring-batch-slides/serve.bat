@echo off
title reveal-js server
call java12.bat
cd C:\Code\git\anderscore\revealjs-server
mvn spring-boot:run -Dspring-boot.run.arguments=--revealjs-server.project-base-dir=C:\Code\git\anderscore\goldschmiede-spring-batch\slides
