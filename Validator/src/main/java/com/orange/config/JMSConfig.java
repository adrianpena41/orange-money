package com.orange.config;

import javax.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JMSConfig {

	@Bean
	public Queue queueTransactions() {
		return new ActiveMQQueue("orange-money-transactions");
	}

	@Bean
	public Queue queueClients() {
		return new ActiveMQQueue("orange-money-clients");
	}

}
