# Stage 1: Build the WAR file using Maven
FROM maven:3.8.5-openjdk-11-slim AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the "app" folder
COPY . .

# Compile the application and generate the WAR file
RUN mvn clean package

# Stage 2: Run the application using Tomcat
FROM tomcat:10-jdk11

# Set the working directory inside the container
WORKDIR /app

# Copy the WAR file from the builder stage to Tomcat's webapps directory
COPY --from=builder /app/target/cs122b-project1-api-example.war /usr/local/tomcat/webapps/cs122b_project1_api_example_war.war

# Expose port 8080 to allow external access to the application
EXPOSE 8080

# Start Tomcat in the foreground
CMD ["catalina.sh", "run"]

# Side note: The final image will only contain the `tomcat` base image, making it smaller and more efficient.
