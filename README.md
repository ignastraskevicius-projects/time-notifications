# Time-Notifications

A little service allowing to subscribe to time notifications at periodic intervals to provided URLs 
via HATEOAS HAL REST API

## Set up

### Requirements
* Java 17
* port 8080 to be free

### 1. Build

./mvnw clean install

### 2. Run

java -jar time-notifications-service/target/time-notifications-service-1.0-SNAPSHOT.jar

#### Usage

* root resource will be available at http://localhost:8080
* Subscription resource structure:
```
{
    "subscriptionUri":"http://urlToInvoke:port",
    "frequency":{
        "amount":4,
        "timeUnit":"second"
    }
}
```
