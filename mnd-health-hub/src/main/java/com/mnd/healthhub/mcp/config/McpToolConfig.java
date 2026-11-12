package com.mnd.healthhub.mcp.config;

import com.mnd.healthhub.mcp.tools.AppointmentMcpTools;
import com.mnd.healthhub.mcp.tools.FlowRegulationMcpTools;
import com.mnd.healthhub.mcp.tools.OrderMcpTools;
import com.mnd.healthhub.mcp.tools.PackageMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider mcpToolProvider(
            PackageMcpTools packageMcpTools,
            AppointmentMcpTools appointmentMcpTools,
            OrderMcpTools orderMcpTools,
            FlowRegulationMcpTools flowRegulationMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(packageMcpTools, appointmentMcpTools,orderMcpTools,flowRegulationMcpTools)
                .build();
    }
}