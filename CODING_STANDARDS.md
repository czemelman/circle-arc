# Java Spring Boot Coding Standards

**Version**: 1.0
**Last Updated**: 2025-10-28
**Purpose**: Generic coding standards applicable to all Java Spring Boot projects

---

## Table of Contents

1. [Compliance Hierarchy](#1-compliance-hierarchy)
2. [Error Handling](#2-error-handling)
3. [Method Structure](#3-method-structure)
4. [Functional Programming](#4-functional-programming)
5. [Null Safety](#5-null-safety)
6. [Dependency Injection](#6-dependency-injection)
7. [Caching Patterns](#7-caching-patterns)
8. [Logging Standards](#8-logging-standards)
9. [Testing Requirements](#9-testing-requirements)
10. [Code Organization](#10-code-organization)
11. [Verification Checklist](#11-verification-checklist)

---

## 1. Compliance Hierarchy

### Priority 1: MANDATORY RULES
Rules marked **[MANDATORY]** MUST be followed in ALL code without exception.

### Priority 2: STRONGLY RECOMMENDED
Rules marked **[STRONGLY RECOMMENDED]** should be followed unless there's a documented, exceptional reason not to.

### Priority 3: PREFERRED
Rules marked **[PREFERRED]** should be followed when possible for consistency.

---

## 2. Error Handling

### [MANDATORY] Rule 2.1: Use Result Types for Fallible Operations

Operations that can fail should return explicit result types (Either, Result, or custom types) instead of throwing exceptions for control flow.

**Rationale**: Makes error handling explicit, enables functional composition, improves testability.

✅ **CORRECT:**
```java
public Either<ErrorInfo, PaymentResponse> processPayment(PaymentRequest request) {
    return validateRequest(request)
        .flatMap(this::executePayment);
}

private Either<ErrorInfo, ValidatedRequest> validateRequest(PaymentRequest request) {
    if (request == null) {
        return Either.left(new ErrorInfo("NULL_REQUEST", "Request cannot be null"));
    }
    if (!request.isValid()) {
        return Either.left(new ErrorInfo("INVALID_REQUEST", "Request validation failed"));
    }
    return Either.right(new ValidatedRequest(request));
}
```

❌ **VIOLATION:**
```java
public PaymentResponse processPayment(PaymentRequest request) throws PaymentException {
    if (!isValid(request)) {
        throw new PaymentException("Invalid request");  // WRONG: Exception for control flow
    }
    return executePayment(request);
}
```

### [MANDATORY] Rule 2.2: Wrap External Exceptions

External API calls (database, HTTP, file I/O) that throw exceptions MUST be wrapped in try-catch or Try.of() patterns.

✅ **CORRECT:**
```java
public Either<ErrorInfo, String> callExternalApi() {
    try {
        String result = externalClient.makeCall();
        return Either.right(result);
    } catch (Exception e) {
        log.error("API call failed", e);
        return Either.left(new ErrorInfo("API_ERROR", e.getMessage()));
    }
}

// Or with Vavr Try:
public Either<ErrorInfo, String> callExternalApi() {
    return Try.of(() -> externalClient.makeCall())
        .onFailure(ex -> log.error("API call failed", ex))
        .toEither()
        .mapLeft(throwable -> new ErrorInfo("API_ERROR", throwable.getMessage()));
}
```

### [STRONGLY RECOMMENDED] Rule 2.3: Initialization Methods Should Be Fail-Safe

Methods annotated with `@PostConstruct` should use fail-safe patterns instead of throwing exceptions.

✅ **CORRECT:**
```java
private volatile boolean serviceReady = false;

@PostConstruct
public void initialize() {
    try {
        loadResources();
        this.serviceReady = true;
        log.info("Service initialized successfully");
    } catch (Exception e) {
        log.error("Initialization failed. Service will deny all requests.", e);
        this.serviceReady = false;
    }
}

public boolean processRequest(Request request) {
    if (!serviceReady) {
        log.error("Service not ready, denying request");
        return false;
    }
    // Process request
}
```

---

## 3. Method Structure

### [MANDATORY] Rule 3.1: Single Return Statement Per Method

Every method MUST have EXACTLY ONE return statement at the method level.

**Rationale**: Improves readability, makes control flow explicit, easier to debug.

✅ **CORRECT:**
```java
public Either<ErrorInfo, ProcessResult> processRequest(Request request) {
    Either<ErrorInfo, ProcessResult> result;

    if (request == null) {
        result = Either.left(new ErrorInfo("NULL_REQUEST"));
    } else if (!request.isValid()) {
        result = Either.left(new ErrorInfo("INVALID_REQUEST"));
    } else {
        ProcessResult processResult = doProcess(request);
        result = Either.right(processResult);
    }

    return result;  // ONLY ONE RETURN
}
```

❌ **VIOLATION:**
```java
public Either<ErrorInfo, ProcessResult> processRequest(Request request) {
    if (request == null) {
        return Either.left(new ErrorInfo("NULL_REQUEST"));  // WRONG: Multiple returns
    }
    if (!request.isValid()) {
        return Either.left(new ErrorInfo("INVALID_REQUEST"));  // WRONG
    }
    return Either.right(doProcess(request));  // WRONG
}
```

**EXCEPTION:** Multiple returns are allowed within lambda expressions:
```java
List<String> results = items.stream()
    .map(item -> {
        if (item.isSpecial()) {
            return item.getSpecialValue();  // OK: Within lambda
        }
        return item.getNormalValue();  // OK: Within lambda
    })
    .collect(Collectors.toList());
```

### [STRONGLY RECOMMENDED] Rule 3.2: Extract Common Logic

When methods share significant identical code, extract common logic into shared methods.

✅ **CORRECT:**
```java
public boolean isAllowed(Request request) {
    return evaluateLimit(request, true);
}

public boolean canPass(Request request) {
    return evaluateLimit(request, false);
}

private boolean evaluateLimit(Request request, boolean consume) {
    // Common logic here
}
```

### [STRONGLY RECOMMENDED] Rule 3.3: Keep Methods Focused

- Methods should do ONE thing
- Aim for 20-30 lines maximum
- Extract complex logic into helper methods

---

## 4. Functional Programming

### [MANDATORY] Rule 4.1: Never Call .get() on Optional/Result Types

NEVER call `.get()` on Optional, Either, Try, or similar monadic types. Use functional methods.

✅ **CORRECT:**
```java
public String processOptional(Optional<String> value) {
    return value
        .map(String::toUpperCase)
        .orElse("DEFAULT");
}

public String processEither(Either<Error, String> value) {
    return value
        .map(String::toUpperCase)
        .getOrElse("DEFAULT");
}
```

❌ **VIOLATION:**
```java
public String processOptional(Optional<String> value) {
    if (value.isPresent()) {
        return value.get().toUpperCase();  // WRONG: Using .get()
    }
    return "DEFAULT";
}
```

### [STRONGLY RECOMMENDED] Rule 4.2: Use Pattern Matching for Multiple Conditions

For 3+ conditional branches, use pattern matching or switch expressions instead of if-else chains.

✅ **CORRECT (Java 17+ with pattern matching):**
```java
private boolean evaluate(Request request) {
    return switch (request.getStatus()) {
        case PENDING -> processPending(request);
        case APPROVED -> processApproved(request);
        case REJECTED -> processRejected(request);
        default -> false;
    };
}
```

✅ **ACCEPTABLE for 2 conditions:**
```java
private boolean evaluate(Request request) {
    if (request == null) {
        return false;
    } else {
        return performEvaluation(request);
    }
}
```

### [PREFERRED] Rule 4.3: Use Method References

Use method references instead of lambdas when possible.

✅ **CORRECT:**
```java
.filter(StringUtils::isNotBlank)
.map(String::toUpperCase)
.map(this::processValue)
```

❌ **AVOID:**
```java
.filter(str -> StringUtils.isNotBlank(str))
.map(x -> x.toUpperCase())
.map(x -> this.processValue(x))
```

---

## 5. Null Safety

### [MANDATORY] Rule 5.1: Use Optional for Nullable Return Values

Methods that might not return a value MUST return `Optional<T>`.

✅ **CORRECT:**
```java
public Optional<User> findUser(String id) {
    User user = userRepository.findById(id);
    return Optional.ofNullable(user);
}

// Usage
findUser(id)
    .map(User::getName)
    .orElse("Unknown");
```

❌ **VIOLATION:**
```java
public User findUser(String id) {
    return userRepository.findById(id);  // WRONG: Might return null
}
```

### [STRONGLY RECOMMENDED] Rule 5.2: Use Optional Chaining

Chain Optional operations instead of nested if-statements.

✅ **CORRECT:**
```java
public String getUserEmail(String userId) {
    return findUser(userId)
        .map(User::getProfile)
        .map(Profile::getEmail)
        .filter(StringUtils::isNotBlank)
        .orElse("no-email@example.com");
}
```

### [MANDATORY] Rule 5.3: Never Return Null

Never return `null` from methods. Use Optional, empty collections, or default values.

✅ **CORRECT:**
```java
public List<User> findUsers() {
    List<User> users = repository.findAll();
    return users != null ? users : Collections.emptyList();
}

public Optional<User> findUser(String id) {
    return Optional.ofNullable(repository.findById(id));
}
```

---

## 6. Dependency Injection

### [MANDATORY] Rule 6.1: Use Constructor Injection

ALWAYS use constructor injection. NEVER use field injection.

✅ **CORRECT:**
```java
@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository repository,
                         NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }
}

// Or with Lombok
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository repository;
    private final NotificationService notificationService;
}
```

❌ **VIOLATION:**
```java
@Service
public class PaymentService {
    @Autowired  // WRONG: Field injection
    private PaymentRepository repository;

    @Autowired  // WRONG: Field injection
    private NotificationService notificationService;
}
```

### [STRONGLY RECOMMENDED] Rule 6.2: Use @ConfigurationProperties for Related Settings

Group related configuration properties using `@ConfigurationProperties`.

✅ **CORRECT:**
```java
@Configuration
@ConfigurationProperties(prefix = "app.payment")
@Data
public class PaymentProperties {
    private int timeout = 30;
    private int maxRetries = 3;
    private boolean enabled = true;
}
```

---

## 7. Caching Patterns

### [STRONGLY RECOMMENDED] Rule 7.1: Use Loading Functions for Cache Access

When using local caches (Caffeine, Guava), prefer loading functions over manual get/put.

✅ **CORRECT:**
```java
@Service
public class UserService {
    private final Cache<String, Optional<User>> userCache;

    @PostConstruct
    public void initializeCache() {
        this.userCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    }

    public Optional<User> getUser(String id) {
        return userCache.get(id, key -> {
            log.info("Cache miss for user: {}", key);
            return repository.findById(key);
        });
    }
}
```

❌ **AVOID:**
```java
public Optional<User> getUser(String id) {
    Optional<User> cached = userCache.getIfPresent(id);
    if (cached != null) {
        return cached;
    }
    Optional<User> user = repository.findById(id);
    userCache.put(id, user);
    return user;
}
```

### [STRONGLY RECOMMENDED] Rule 7.2: Cache Optional Values Directly

When caching nullable values, cache the Optional itself.

✅ **CORRECT:**
```java
private Cache<String, Optional<Data>> cache;

public Optional<Data> getData(String id) {
    return cache.get(id, key -> repository.findById(key));
}
```

---

## 8. Logging Standards

### [MANDATORY] Rule 8.1: Use SLF4J with Lombok

Use SLF4J for logging via Lombok's `@Slf4j` annotation.

✅ **CORRECT:**
```java
@Slf4j
@Service
public class PaymentService {
    public void processPayment(Payment payment) {
        log.info("Processing payment: id={}", payment.getId());
        try {
            executePayment(payment);
            log.info("Payment processed successfully: id={}", payment.getId());
        } catch (Exception e) {
            log.error("Payment processing failed: id={}", payment.getId(), e);
        }
    }
}
```

### [STRONGLY RECOMMENDED] Rule 8.2: Use Parameterized Logging

Always use parameterized logging, never string concatenation.

✅ **CORRECT:**
```java
log.info("Processing request: id={}, type={}", requestId, type);
```

❌ **AVOID:**
```java
log.info("Processing request: id=" + requestId + ", type=" + type);
```

### [STRONGLY RECOMMENDED] Rule 8.3: Log at Appropriate Levels

- **ERROR**: Exceptions and critical failures
- **WARN**: Recoverable issues, degraded functionality
- **INFO**: Important business events, state changes
- **DEBUG**: Detailed diagnostic information
- **TRACE**: Very detailed diagnostic information

---

## 9. Testing Requirements

### [MANDATORY] Rule 9.1: Use AssertJ for Assertions

ALWAYS use AssertJ for assertions. NEVER use JUnit assertions.

✅ **CORRECT:**
```java
import static org.assertj.core.api.Assertions.assertThat;

@Test
void shouldProcessPayment() {
    // Arrange
    Payment payment = createTestPayment();

    // Act
    PaymentResult result = service.processPayment(payment);

    // Assert
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getTransactionId()).isNotNull();
    assertThat(result.getAmount()).isEqualTo(payment.getAmount());
}
```

❌ **VIOLATION:**
```java
@Test
void shouldProcessPayment() {
    PaymentResult result = service.processPayment(payment);
    assertTrue(result.isSuccess());  // WRONG: JUnit assertion
    assertNotNull(result.getTransactionId());  // WRONG
    assertEquals(payment.getAmount(), result.getAmount());  // WRONG
}
```

### [MANDATORY] Rule 9.2: Use Spring Test Utilities

Use Spring's test utilities instead of manual reflection.

✅ **CORRECT:**
```java
import org.springframework.test.util.ReflectionTestUtils;

@Test
void testPrivateField() {
    ReflectionTestUtils.setField(service, "enabled", false);
    assertThat(service.isEnabled()).isFalse();
}
```

❌ **VIOLATION:**
```java
@Test
void testPrivateField() throws Exception {
    Field field = Service.class.getDeclaredField("enabled");
    field.setAccessible(true);
    field.set(service, false);
}
```

### [STRONGLY RECOMMENDED] Rule 9.3: Test Naming Convention

Use descriptive test names: `methodName_condition_expectedResult()`

✅ **CORRECT:**
```java
@Test
void processPayment_withValidRequest_returnsSuccess() { }

@Test
void processPayment_withInvalidAmount_returnsError() { }

@Test
void processPayment_whenServiceDown_returnsFallback() { }
```

### [STRONGLY RECOMMENDED] Rule 9.4: Arrange-Act-Assert Structure

Organize tests with clear sections and blank lines between them.

✅ **CORRECT:**
```java
@Test
void shouldCalculateTotal() {
    // Arrange
    Order order = new Order();
    order.addItem(new Item("A", 10.0));
    order.addItem(new Item("B", 20.0));

    // Act
    BigDecimal total = order.calculateTotal();

    // Assert
    assertThat(total).isEqualByComparingTo("30.0");
}
```

### [MANDATORY] Rule 9.5: Use Unique Test Data

Generate unique test data IDs to prevent parallel test interference.

✅ **CORRECT:**
```java
private static final String TEST_PREFIX = "TEST_";
private final String testRunId = UUID.randomUUID().toString().substring(0, 8);
private final String testUserId = TEST_PREFIX + "USER_" + testRunId;
```

---

## 10. Code Organization

### [STRONGLY RECOMMENDED] Rule 10.1: Package Structure

Organize code by feature or layer:

```
com.example.project/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic
├── repository/       # Data access
├── model/           # Domain models
│   ├── entity/      # Database entities
│   └── dto/         # Data transfer objects
├── exception/       # Custom exceptions
└── util/            # Utility classes
```

### [STRONGLY RECOMMENDED] Rule 10.2: Class Responsibilities

- **Controllers**: HTTP handling, request/response mapping
- **Services**: Business logic, transaction management
- **Repositories**: Data access, database operations
- **DTOs**: Data transfer between layers
- **Entities**: Database mapping, persistence

### [PREFERRED] Rule 10.3: Use Immutable Objects

Prefer immutable objects for DTOs and value objects.

✅ **CORRECT:**
```java
@Value
@Builder
public class PaymentRequest {
    String transactionId;
    BigDecimal amount;
    String currency;
}
```

---

## 11. Verification Checklist

Before finalizing code, verify:

### Error Handling
- [ ] All fallible operations return explicit result types
- [ ] External API calls are wrapped in exception handling
- [ ] No exceptions thrown for control flow

### Method Structure
- [ ] Every method has exactly one return statement (except lambdas)
- [ ] Methods are focused and under 30 lines
- [ ] No code duplication

### Functional Programming
- [ ] No `.get()` calls on Optional/Either/Try
- [ ] Pattern matching used for 3+ conditions
- [ ] Method references used where possible

### Null Safety
- [ ] Nullable return values use Optional
- [ ] No null returns
- [ ] Optional chaining used appropriately

### Dependency Injection
- [ ] Constructor injection used exclusively
- [ ] No field injection
- [ ] Configuration properties grouped appropriately

### Testing
- [ ] AssertJ used for all assertions
- [ ] Spring test utilities used (ReflectionTestUtils)
- [ ] Unique test data IDs
- [ ] Clear Arrange-Act-Assert structure

### Logging
- [ ] SLF4J used via @Slf4j
- [ ] Parameterized logging used
- [ ] Appropriate log levels

---

## Quick Reference: Common Violations

| Violation | Fix |
|-----------|-----|
| Multiple returns | Single return with result variable |
| Using .get() on Optional | Use .map(), .flatMap(), .orElse() |
| Field injection | Constructor injection |
| JUnit assertions | AssertJ assertThat() |
| Throwing for control flow | Return Either.left() or Optional.empty() |
| Null returns | Return Optional or empty collection |
| Manual reflection | Use ReflectionTestUtils |
| String concatenation in logs | Parameterized logging |

---

**Document Version**: 1.0
**Applicable To**: All Java Spring Boot projects
**Review Cycle**: Quarterly
