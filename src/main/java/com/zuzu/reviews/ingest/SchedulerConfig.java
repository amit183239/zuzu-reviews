package com.zuzu.reviews.ingest;

import com.zuzu.reviews.config.IngestionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
public class SchedulerConfig {

    // Allows cron to be read from properties at runtime
    @Bean(name = "injectionPropsExpression")
    public Expression injectionPropsExpression(IngestionProperties props) {
        ExpressionParser parser = new SpelExpressionParser();
        return parser.parseExpression("'" + props.getPollCron() + "'");
    }
}
