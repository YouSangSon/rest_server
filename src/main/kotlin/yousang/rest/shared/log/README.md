# Simplified Logging System

This package provides a simplified logging system for the application. It focuses on core functionality while eliminating redundant or complex features.

## Core Features

1. **LoggerDelegate** - Easy logger instantiation with Kotlin property delegate syntax
   ```kotlin
   private val log by LoggerDelegate()
   ```

2. **Extension functions** - Simple extensions for common logging patterns
   ```kotlin
   // Logging with context
   log.logWithContext("info", "User created", mapOf("userId" to userId))
   
   // Time measurement
   log.withTiming("Database operation") {
     // your code here
   }
   
   // Conditional logging
   log.ifDebug {
     debug("Details: $complexObject")
   }
   ```

3. **@Loggable annotation** - Automatic method logging with AOP
   ```kotlin
   @Loggable
   fun processOrder(order: Order): OrderResult {
     // method implementation
   }
   ```

## Basic Usage

```kotlin
class MyService {
  private val log by LoggerDelegate()
  
  fun doSomething() {
    log.info("Starting operation")
    
    // Your business logic here
    
    log.info("Operation completed")
  }
}
```

Or use the extension property:

```kotlin
class MyService {
  fun doSomething() {
    log.info("Starting operation")
    // Your business logic here
  }
}
``` 