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
