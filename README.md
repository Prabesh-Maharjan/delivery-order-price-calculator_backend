# Delivery Order Price Calculator (DOPC) - Ktor-Based Kotlin Project

-This project is generated using Ktor Project Generator,to create RESTful services using Kotlin to implement a service for calculating the total delivery price of an order.

## Prerequisites
Ensure the following are installed on system:
- **Java 11 or higher** (required to run Ktor): Check installation with `java -version`.
- **Kotlin** (required to compile the application): Check installation with `kotlin -version`.
- **Gradle** (used as the build tool): Use the Gradle wrapper if Gradle is not installed.


## Installation

1. Download the ZIP file of the project from the provided Dropbox link.
2. Extract the ZIP file to a directory on your local machine.
3. Navigate to the project directory.
4. Build the project using the Gradle wrapper:
   ```bash
   ./gradlew build
## Running the Application

1. **Run with Gradle**  
   Use the Gradle wrapper to start the application:
   ```bash
   ./gradlew run
2. If using IntelliJ IDEA (optional),then locate src/main/kotlin/Application.kt, and run it directly from the IDE.

3. Access the API
Once the application is running, the API will be available at http://localhost:8080

The API provides a single endpoint to calculate the delivery order price.
## API Documentation

### Endpoint: `/api/v1/delivery-order-price`

#### Method: `GET`

#### Request Example:
```plaintext
http://localhost:8080/api/v1/delivery-order-price?venue_slug=home-assignment-venue-helsinki&cart_value=15&user_lat=24.92813512&user_lon=60.17012143
 ```
#### Query Parameters:
| Parameter     | Type     | Description                 | Required |
| ------------- | -------- |-----------------------------| -------- |
| `venue_slug`  | `String` | Unique identifier for venue | Yes      |
| `cart_value`  | `Integer`| Value of the order's cart   | Yes      |
| `user_lat`    | `Double` | Latitude of the user        | Yes      |
| `user_lon`    | `Double` | Longitude of the user       | Yes      |

#### Success Response:
Instead of just returning data as specified in assignmnet, it is returned alongside errorCode and message as well for uniform response(if other endpoint added)

{
    "errorCode": "0",
    "statusCode": "200",
    "message": "Success",
    "data": {
        "total_price": 1190,
        "small_order_surcharge": 985,
        "cart_value": 15,
        "delivery": {
            "fee": 190,
            "distance": 0
        }
    }
}

#### Error Response:
{
"errorCode": "1",
"statusCode": "400",
"message": "Delivery in this location is not possible[Estimated distance:201669 which is too far]"
}

## Testing

1. Run all unit and integration tests using Gradle:
   ```bash
   ./gradlew test
2. If using IntelliJ IDEA (optional),then navigate to src/test/kotlin/ApplicationTest.kt and run the tests directly from the IDE.


## License

This project is not licensed as it is just assignment.
