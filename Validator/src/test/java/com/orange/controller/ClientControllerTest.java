package com.orange.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.jms.Queue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.entity.Client;

@RunWith(SpringRunner.class)
@WebMvcTest
public class ClientControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private Queue queue;

	@MockBean
	private JmsTemplate jmsTemplate;

	@Test
	public void testCreateClient_happyPath() throws Exception {
		Client client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011");
		client.setName("Adrian Pena");
		client.setIban("RO19RZBR8637876275195736");
		client.setWallet("0721306901");

		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testCreateClient_invalidWallet() throws Exception {
		Client client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011");
		client.setName("Adrian Pena");
		client.setIban("RO19RZBR8637876275195736");
		client.setWallet("AS0721306901");

		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Client is invalid. Wallet is invalid!"));
	}
	
	@Test
	public void testCreateClient_invalidIban() throws Exception {
		Client client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011");
		client.setName("Adrian Pena");
		client.setIban("AAARO19RZBR8637876275195736");
		client.setWallet("0721306901");

		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Client is invalid. Iban is invalid!"));
	}
	
	@Test
	public void testCreateClient_invalidName() throws Exception {
		Client client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011");
		client.setName("123Adrian Pena");
		client.setIban("RO19RZBR8637876275195736");
		client.setWallet("0721306901");

		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Client is invalid. Name is invalid!"));
	}
	
	@Test
	public void testCreateClient_invalidCnp() throws Exception {
		Client client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011123");
		client.setName("Adrian Pena");
		client.setIban("RO19RZBR8637876275195736");
		client.setWallet("0721306901");

		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Client is invalid. Cnp is invalid!"));
	}
	
	@Test
	public void testCreateClient_invalidAll() throws Exception {
		Client client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011123");
		client.setName("123Adrian Pena");
		client.setIban("123RO19RZBR8637876275195736");
		client.setWallet("1230721306901");

		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Client is invalid. Cnp is invalid!Name is invalid!Iban is invalid!Wallet is invalid!"));
	}

}
