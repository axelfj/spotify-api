# 🎧 Spotify Track Metadata API

A lightweight Spring Boot demo that connects to the Spotify API, fetches track metadata using ISRC codes, and stores them temporarily using an in-memory H2 database. You'll need to have Java 21 with the proper JDK, and it's recommended to run it on IntelliJ.

## 🚀 Features

- Fetch track metadata from Spotify by ISRC
- Download and store album cover images (byte[])
- Persist results in an in-memory H2 database

## 🔒 Credentials

- Set up your credentials for API/Spotify usage.
- Basic Auth for the API: 
  - username: admin 
  - password: password
- Go into https://developer.spotify.com/dashboard and create a new app.
- Get the **client id** and **client secret** and put them in your env variables.

## 🎮 Play with it

- Navigate to https://www.isrcfinder.com/ to find your favorite track ISRC.
- Use Postman or Curl, or your favorite tool to test the different endpoints.
- POST /track/metadata?isrc=<your_isrc>
- GET /track/metadata?isrc=<your_isrc>
- GET /track/cover?isrc=<your_isrc>
- Change <your_isrc> with your desired ISRC and interact with the app.

## 📃 Swagger

- Navigate to http://localhost:8080/swagger-ui/index.html#/ to check the endpoints
