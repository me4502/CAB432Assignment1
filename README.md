# CAB432Assignment1
Assignment 1 for CAB432 (Cloud Computing) at QUT, a subject on Docker and cloud APIs.

## Creating a Docker Image

Before creating the docker image, make sure you've setup the required API keys in the `photo_app.conf` file, this gets copied to the container.

In order to create a docker image, make sure you have docker installed, and run `./gradlew docker --info` (Or `gradlew docker --info` on Windows).

This will automatically generate the docker image, which may take a while. The image will be registered with your docker install.
