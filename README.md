# spark-project

Using Java (Spring Boot framework) and PostgreSQL database, implemented REST APIs which enable logged in users to manipulate their own photos (get, add, update, delete, etc.).

## About application
You can register user (every field is validated btw) and login to get JWT token which will allow you/user to add one photo, add multiple photos, get one photo, get multiple photos based on their created date (and sort them by size if you wish to do so), sort all photos by size, update one photo and delete one photo. (just like in registration most stuff is validated ex. you can only upload files which are images, you can't upload same image twice etc. There are comments in the app [commentedCode branch only] which explain pretty much everything you need to know about the app)

## commentedCode branch
I added this branch mainly so the code doesn't look messy and unreadable because i made comments for most lines of code

## Prerequisites

JAVA

IDE (IntelliJ or eclipse)

POSTGRESQL

POSTMAN

DOCKER (optional)

# How to use app

## Set up database
Before starting you will need to create postgres db named spark_java (or you can name it differently, in that case though you will also need to change the name of the db in application.yml)

## Running the application
Open application folder using some java IDE (like intellij or eclipse) and run it through said IDE or you can use docker (docker-compose up -d)

## Testing Endpoints
There is postman collection file in main application folder which you can import and use to test different endpoints. Some endpoints are just examples and you will need to change stuff in them for yourself (like name of the image to test get image endpoint etc.)

# IMPORTANT ABOUT SENDING EMAILS FOR PASSWORD RESET

You need to add your own gmail adress and app password to be able to send emails to the user, you need to add them in application.yml, the lines where you need to add them are commented so they are easy to spot. (for how to create app password follow these guidlines https://support.google.com/accounts/answer/185833?hl=en#:%7E:text=An%20App%20Password%20is%20a,2%2DStep%20Verification%20turned%20on.).

## TODO Unit Tests

## Authors

* **Tomislav Lovrić** - [spark-project](https://github.com/Tomislav-lovric/spark-java)
