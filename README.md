# Spring Bot Manager

Spring Boot **modular monolith** service for a multi-tenant Bot-as-a-Service platform (WhatsApp Cloud API).

## Features

- **Multi-bot routing** via `phone_number_id`
- **Configuration-driven conversation flows** (JSON)
- **Redis-backed state** with in-memory fallback for graceful degradation
- **Payment provider abstraction** (CamPay/MTN MoMo)
- **MQTT integration** for IoT machine control (HiveMQ client)
- **Internationalization** (EN/FR) with template rendering
- **Business hours validation** with timezone support
- **Rate limiting** middleware (token bucket)
- **Webhook signature verification** (HMAC-SHA256)

## Bots Included

### LaundryBot
Self-service laundromat chatbot with:
- Bilingual support (English/French)
- Machine selection (manual ID or list)
- Cycle selection with business hours validation
- Mobile money payment integration
- Feedback collection system
- Staff alerts for low ratings

### ThomasNetworkBot
Network access service bot for pressing/laundry services.

## Tech Stack

- **Java 17** + **Spring Boot 3.2.2**
- **Maven** build system
- **Redis** for state management
- **HiveMQ MQTT Client** for IoT
- **Mustache** for template rendering
- **Lombok** for boilerplate reduction

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Redis (optional, falls back to in-memory)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/GustaveDjoutsop/spring-bot-manager.git
   cd spring-bot-manager
   ```

2. Configure environment:
   ```bash
   cp .env.example .env
   # Edit .env with your values
   ```

3. Build:
   ```bash
   mvn clean package -DskipTests
   ```

4. Run:
   ```bash
   java -jar target/spring-bot-manager-1.0.0-SNAPSHOT.jar
   ```

   Or with Maven:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | HTTP server port | `3000` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `WHATSAPP_ACCESS_TOKEN_LAUNDRY` | WhatsApp access token for LaundryBot | - |
| `CAMPAY_USERNAME` | CamPay API username | - |
| `CAMPAY_PASSWORD` | CamPay API password | - |
| `MQTT_BROKER_URL` | MQTT broker URL | - |

### Bot Configuration

Bots are configured via JSON files in `configs/bots/`:

```json
{
  "botId": "laundry",
  "botName": "Smart Laundry",
  "botType": "laundry",
  "phoneNumberId": "YOUR_PHONE_NUMBER_ID",
  "verifyToken": "YOUR_VERIFY_TOKEN",
  "shortCycle": { "duration": 30, "price": 1000, "pulseCount": 1 },
  "longCycle": { "duration": 60, "price": 2000, "pulseCount": 2 },
  "businessHours": {
    "openTime": "07:00",
    "closeTime": "22:00",
    "timezone": "Africa/Douala"
  }
}
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/health` | Health check |
| `GET` | `/api/health/ready` | Readiness probe |
| `GET` | `/api/health/live` | Liveness probe |
| `GET` | `/api/whatsapp/webhook` | Meta verification |
| `POST` | `/api/whatsapp/webhook` | WhatsApp inbound messages |
| `POST` | `/api/payments/webhook` | Payment provider callbacks |
| `GET` | `/api/machines/{botId}` | List machines for a bot |

## Docker

### Build

```bash
docker build -t spring-bot-manager:latest .
```

### Run

```bash
docker run -p 3000:3000 --env-file .env spring-bot-manager:latest
```

## Kubernetes Deployment

Helm charts are available in `ci/helm-chart/`:

```bash
helm install spring-bot-manager ./ci/helm-chart \
  -f ./ci/helm-values/prod.yaml \
  --set secrets.whatsappAccessToken=$WHATSAPP_TOKEN
```

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
cd integration-tests
docker-compose up --build
```

## Project Structure

```
src/main/java/com/botmanager/
├── bots/
│   ├── laundry/          # LaundryBot implementation
│   └── thomasnetwork/    # ThomasNetworkBot implementation
├── config/               # Configuration properties
├── controller/           # REST controllers
├── core/
│   ├── bot/              # Bot base classes and registry
│   ├── flow/             # Conversation flow engine
│   ├── i18n/             # Internationalization
│   ├── machine/          # Machine management
│   ├── mqtt/             # MQTT client
│   ├── payment/          # Payment gateway
│   ├── queue/            # Message queue
│   ├── redis/            # Redis manager
│   └── whatsapp/         # WhatsApp client
├── handler/              # Webhook handlers
├── middleware/           # Request filters
└── util/                 # Utilities
```

## Related Projects

- [BotManagerService](https://github.com/GustaveDjoutsop/BotManagerService) - Node.js variant
- [SmartLaundromatControlSystem](https://github.com/GustaveDjoutsop/SmartLaundromatControlSystem) - Original laundromat system

## License

MIT
