# API Testing Hub Backend

A high-performance, Spring Boot-powered backend service for API testing, documentation generation, and analysis.

## Features

- **API Request Execution**: Execute HTTP requests through a CORS proxy
- **AI-Powered Documentation**: Generate comprehensive API documentation using OpenAI GPT models
- **Response Analysis**: Analyze API responses for insights and troubleshooting
- **URL Validation**: Validate URLs for safety and accessibility

## Tech Stack

- Java 17
- Spring Boot 3.5.0
- Spring WebFlux for reactive programming
- Caffeine for caching
- Spring Security
- OpenAPI Documentation (Springdoc)

## Prerequisites

- JDK 17+
- Gradle 8.10+
- OpenAI API key (for AI documentation features)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yourusername/api-testing-hub-backend.git
cd api-testing-hub-backend
```

### Configure Environment Variables

Copy the example environment file and configure your settings:

```bash
cp env.example .env
```

Edit `.env` to include your OpenAI API key and other configuration:

```properties
# API Configuration
SERVER_PORT=8080
BASE_URL=http://localhost:8080

# OpenAI Configuration
OPENAI_API_KEY=your-api-key-here
OPENAI_MODEL=gpt-4
OPENAI_TEMPERATURE=0.7
OPENAI_MAX_TOKENS=4000

# Security Configuration
ALLOWED_ORIGINS=*
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_DURATION=60
```

### Build and Run

```bash
./gradlew bootRun
```

For production builds:

```bash
./gradlew clean build
java -jar build/libs/api-testing-hub-backend-1.0.0.jar
```

## API Documentation

Once running, access the Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```

## Key Endpoints

### API Request Execution

```http
POST /requests/execute
```

Execute an HTTP request with full request configuration.

### AI Documentation

```http
POST /ai-docs/describe
POST /ai-docs/analyze
POST /ai-docs/documentation
```

Generate API descriptions, analyze responses, and create comprehensive documentation.

## Architecture

The service follows a clean architecture pattern:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic
- **DTOs**: Data Transfer Objects for request/response handling
- **Exceptions**: Centralized exception handling
- **Config**: Application configuration

## Development

### Running Tests

```bash
./gradlew test
```

### Code Style

This project follows Google Java Style Guide. To check style:

```bash
./gradlew checkstyleMain
```

## Deployment

### Docker

```bash
docker build -t api-testing-hub-backend:latest .
docker run -p 8080:8080 --env-file .env api-testing-hub-backend:latest
```

### Kubernetes

Kubernetes deployment manifests are available in the `k8s/` directory.

## Performance

- Reactive programming model for high throughput
- Caffeine caching for improved response times
- Async request processing

## Security

- Input validation
- URL safety checks
- Rate limiting
- CORS configuration

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please use the GitHub issue tracker.

---

© 2025 API Testing Hub Team 
