package com.demo.agentflow.orchestration;

import io.github.asekka.springai.agents.core.Agent;
import io.github.asekka.springai.agents.core.AgentContext;
import io.github.asekka.springai.agents.core.AgentResult;
import io.github.asekka.springai.agents.graph.AgentGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.asekka.springai.agents.core.StateKey;
import org.springframework.ai.chat.messages.Message;
import java.util.List;
import java.util.Map;

@Configuration
public class CustomerSupportGraph {

    private static final Logger log = LoggerFactory.getLogger(CustomerSupportGraph.class);

    public static final StateKey<String> INTENT = StateKey.of("intent", String.class);
    public static final StateKey<String> SENTIMENT = StateKey.of("sentiment", String.class);
    public static final StateKey<String> ORDER_STATUS = StateKey.of("orderStatus", String.class);
    public static final StateKey<String> DECISION = StateKey.of("decision", String.class);

    @Bean
    public AgentGraph supportGraph(ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder.build();

        // 1. Triage Agent: Extract intent and sentiment
        Agent triageAgent = context -> {
            List<Message> msgs = context.messages();
            String userInput = msgs.isEmpty() ? "" : msgs.get(msgs.size() - 1).getContent();
            
            String response = chatClient.prompt()
                    .system("You are a customer support triage agent. Read the user message and extract INTENT and SENTIMENT. " +
                            "Output MUST be exactly two lines:\nINTENT: [REFUND|COMPLAINT|QUESTION]\nSENTIMENT: [ANGRY|NEUTRAL|HAPPY]")
                    .user(userInput)
                    .call()
                    .chatResponse().getResult().getOutput().getContent();
            
            String intent = response.contains("REFUND") ? "REFUND" : "COMPLAINT";
            String sentiment = response.contains("ANGRY") ? "ANGRY" : "NEUTRAL";
            
            log.info("[TRIAGE] -> Intent: {}, Sentiment: {}", intent, sentiment);
            
            return AgentResult.builder()
                    .text(response)
                    .stateUpdates(Map.of(INTENT, intent, SENTIMENT, sentiment))
                    .build();
        };

        // 2. Lookup Agent (Mocking a tool/DB call)
        Agent lookupAgent = context -> {
            // In reality, this would query a database using the customer ID
            String status = "LOST_IN_TRANSIT";
            
            log.info("[LOOKUP] -> Status: {}", status);
            
            return AgentResult.builder()
                    .text("Checked order status.")
                    .stateUpdates(Map.of(ORDER_STATUS, status))
                    .build();
        };

        // 3. Policy Agent (DETERMINISTIC - Pure Java rules engine)
        Agent policyAgent = context -> {
            String intent = context.get(INTENT) != null ? context.get(INTENT) : "UNKNOWN";
            String status = context.get(ORDER_STATUS) != null ? context.get(ORDER_STATUS) : "UNKNOWN";
            
            String decision;
            if ("REFUND".equals(intent) && "LOST_IN_TRANSIT".equals(status)) {
                decision = "FULL_REFUND_WITH_COUPON";
            } else {
                decision = "ESCALATE_TO_HUMAN";
            }
            
            log.info("[POLICY] -> Decision: {}", decision);
            
            return AgentResult.builder()
                    .text("Policy applied.")
                    .stateUpdates(Map.of(DECISION, decision))
                    .build();
        };

        // 4. Writer Agent: Generates final response
        Agent writerAgent = context -> {
            List<Message> msgs = context.messages();
            String userInput = msgs.isEmpty() ? "" : msgs.get(0).getContent();
            String intent = context.get(INTENT) != null ? context.get(INTENT) : "";
            String sentiment = context.get(SENTIMENT) != null ? context.get(SENTIMENT) : "";
            String decision = context.get(DECISION) != null ? context.get(DECISION) : "";
            
            String prompt = String.format("""
                Write a customer support reply based on:
                Original Message: %s
                Detected Sentiment: %s
                Action to take: %s
                
                Make it professional. If angry, apologize profusely. If refund approved, explain they get a full refund + 20%% coupon.
                """, userInput, sentiment, decision);
                
            String response = chatClient.prompt()
                    .system("You are an expert customer support representative.")
                    .user(prompt)
                    .call()
                    .chatResponse().getResult().getOutput().getContent();
                    
            log.info("[WRITER] -> Generated response.");
            
            return AgentResult.builder()
                    .text(response)
                    .stateUpdates(Map.of(
                        INTENT, intent,
                        SENTIMENT, sentiment,
                        DECISION, decision,
                        ORDER_STATUS, context.get(ORDER_STATUS) != null ? context.get(ORDER_STATUS) : ""
                    ))
                    .build();
        };

        return AgentGraph.builder()
                .name("multi-agent-customer-ops")
                .addNode("triage", triageAgent)
                .addNode("lookup", lookupAgent)
                .addNode("policy", policyAgent)
                .addNode("writer", writerAgent)
                .addEdge("triage", "lookup")
                .addEdge("lookup", "policy")
                .addEdge("policy", "writer")
                .build();
    }
}
