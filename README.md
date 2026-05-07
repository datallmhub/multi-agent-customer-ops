---
title: Multi Agent Customer Ops
emoji: 🤖
colorFrom: blue
colorTo: indigo
sdk: docker
pinned: false
---

# Agentic Customer Support Orchestrator

This is a demonstration of the [spring-agent-flow](https://github.com/datallmhub/spring-agent-flow) open-source framework.

It orchestrates a complex Customer Support pipeline in Java using Spring Boot and Mistral AI, showcasing how to mix non-deterministic AI agents with deterministic Java rules (Policy Engine).

### Workflow:
1. **Triage Agent** (LLM): Extracts intent and sentiment.
2. **Lookup Agent** (Tool/Code): Mocks an order status check.
3. **Policy Engine** (Pure Java Code): Evaluates the state and makes a refund/escalation decision without AI hallucinations.
4. **Writer Agent** (LLM): Drafts the perfect empathetic response.

The state is strongly typed (`SupportState`) and passed automatically across nodes.

## Technical Stack


- **Core**: [Spring Boot 3.2.4](https://spring.io/projects/spring-boot)
- **AI Orchestration**: [spring-agent-flow](https://github.com/datallmhub/spring-agent-flow) (Graph-based Multi-Agent framework)
- **LLM Integration**: [Spring AI](https://spring.io/projects/spring-ai) (Mistral AI / Groq / OpenAI)
- **Runtime**: Java 17+
- **Frontend**: Vanilla JS, HTML5, [Tailwind CSS](https://tailwindcss.com/)
- **Streaming**: Server-Sent Events (SSE) for real-time graph visualization
- **Deployment**: Docker on Hugging Face Spaces

## How to run locally

To test this demo on your machine, you will need an API key from an LLM provider (Mistral AI or Groq are recommended for their speed and compatibility).

1. **Clone the repository**
2. **Get an API Key**:
   * Create an account at [Mistral AI Console](https://console.mistral.ai/) or [Groq Console](https://console.groq.com/).
   * Generate an API Key.
3. **Run the application**:
   Set the following environment variables before starting:
   ```bash
   export SPRING_AI_OPENAI_API_KEY="your_api_key_here"
   export SPRING_AI_OPENAI_BASE_URL="https://api.mistral.ai" # Or https://api.groq.com/openai/v1
   mvn spring-boot:run
   ```
4. **Access the UI**: Open `http://localhost:8080` in your browser.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

