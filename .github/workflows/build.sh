#!/bin/bash

# Копирование Dockerfile
cp shareit-gateway/Dockerfile shareit-gateway/
cp shareit-server/Dockerfile shareit-server/

# Сборка проекта
mvn clean verify