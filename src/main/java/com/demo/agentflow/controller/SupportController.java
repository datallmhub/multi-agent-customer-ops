package com.demo.agentflow.controller;

import com.demo.agentflow.model.SupportState;
import io.github.asekka.springai.agents.core.AgentContext;
import io.github.asekka.springai.agents.core.AgentEvent;
import io.github.asekka.springai.agents.graph.AgentGraph;
import io.github.asekka.springai.agents.core.StateKey;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final AgentGraph supportGraph;

    public SupportController(AgentGraph supportGraph) {
        this.supportGraph = supportGraph;
    }

    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> processSupportTicket(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        
        AgentContext context = AgentContext.empty()
                .withMessage(new UserMessage(message));

        return supportGraph.invokeStream(context)
                .map(event -> {
                    // Convert internal AgentEvent to a JSON string for SSE
                    if (event instanceof AgentEvent.NodeTransition t) {
                        return String.format("{\"type\":\"transition\", \"from\":\"%s\", \"to\":\"%s\"}", t.from(), t.to());
                    } else if (event instanceof AgentEvent.Completed c) {
                        String text = c.result().text().replace("\"", "\\\"").replace("\n", "\\n");
                        
                        Map<StateKey<?>, Object> state = c.result().stateUpdates();
                        String intent = state.getOrDefault(com.demo.agentflow.orchestration.CustomerSupportGraph.INTENT, "UNKNOWN").toString();
                        String sentiment = state.getOrDefault(com.demo.agentflow.orchestration.CustomerSupportGraph.SENTIMENT, "UNKNOWN").toString();
                        String orderStatus = state.getOrDefault(com.demo.agentflow.orchestration.CustomerSupportGraph.ORDER_STATUS, "UNKNOWN").toString();
                        String decision = state.getOrDefault(com.demo.agentflow.orchestration.CustomerSupportGraph.DECISION, "UNKNOWN").toString();
                        
                        return String.format("{\"type\":\"completed\", \"response\":\"%s\", \"state\":{\"intent\":\"%s\", \"sentiment\":\"%s\", \"orderStatus\":\"%s\", \"decision\":\"%s\"}}", 
                                text, intent, sentiment, orderStatus, decision);
                    }
                    return "{\"type\":\"ping\"}";
                });
    }
}
