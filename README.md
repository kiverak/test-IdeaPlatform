# Flight Data Analyzer

A simple Java command-line application that analyzes flight ticket data from a JSON file.

The application performs the following calculations:
- For flights between Vladivostok (VVO) and Tel Aviv (TLV), it determines the minimum flight time for each airline carrier.
- It calculates the difference between the average and the median price of all tickets in the dataset.

## Prerequisites

Before you begin, ensure you have the following installed on your Linux system:

- [Git](https://git-scm.com/)
- [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or a later version.
- [Apache Maven](https://maven.apache.org/)

You can verify your installations by running the following commands in your terminal:

```bash
java -version
mvn -version
```

## How to Launch

Follow these steps to build and run the application from your terminal.

### 1. Clone the Repository

Open your terminal and clone the project to your local machine.

```bash
# Replace <repository_url> with the actual URL of your Git repository
git clone <repository_url>

# Navigate into the project directory
cd test-IdeaPlatform
```

### 2. Build the Project

```bash
mvn clean package
```

### 3. Run the Application

```bash
java -jar target/test-IdeaPlatform-1.0.jar 
```
