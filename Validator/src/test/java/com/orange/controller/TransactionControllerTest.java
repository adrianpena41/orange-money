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
import com.orange.entity.Transaction;
import com.orange.entity.Transaction.TransactionType;

@RunWith(SpringRunner.class)
@WebMvcTest
public class TransactionControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private Queue queue;

	@MockBean
	private JmsTemplate jmsTemplate;

	@Test
	public void testCreateTransaction_happyPath() throws Exception {
		Client client1 = new Client();
		client1.setClientId(100L);
		client1.setCnp("1940407110011");
		client1.setName("Adrian Pena");
		client1.setIban("RO19RZBR8637876275195736");
		client1.setWallet("0721306901");

		Client client2 = new Client();
		client2.setClientId(101L);
		client2.setCnp("1940407110022");
		client2.setName("Ion Ionescu");
		client2.setIban("RO19RZBR8637876275195737");
		client2.setWallet("0721306902");

		Transaction transaction = new Transaction();
		transaction.setAmount("100");
		transaction.setDescription("Datorie");
		transaction.setClientFrom(client1);
		transaction.setClientTo(client2);
		transaction.setType(TransactionType.IBAN_TO_IBAN);
		transaction.setTransactionId(100L);

		ObjectMapper mapper = new ObjectMapper();
		String transactionString = mapper.writeValueAsString(transaction);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON)
						.content(transactionString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testCreateTransaction_invalidAmount() throws Exception {
		Client client1 = new Client();
		client1.setClientId(100L);
		client1.setCnp("1940407110011");
		client1.setName("Adrian Pena");
		client1.setIban("RO19RZBR8637876275195736");
		client1.setWallet("0721306901");

		Client client2 = new Client();
		client2.setClientId(101L);
		client2.setCnp("1940407110022");
		client2.setName("Ion Ionescu");
		client2.setIban("RO19RZBR8637876275195737");
		client2.setWallet("0721306902");

		Transaction transaction = new Transaction();
		transaction.setAmount("100asd");
		transaction.setDescription("Datorie");
		transaction.setClientFrom(client1);
		transaction.setClientTo(client2);
		transaction.setType(TransactionType.IBAN_TO_IBAN);
		transaction.setTransactionId(100L);

		ObjectMapper mapper = new ObjectMapper();
		String transactionString = mapper.writeValueAsString(transaction);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON)
						.content(transactionString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest()).andExpect(
						MockMvcResultMatchers.jsonPath("$.error").value("Transaction is invalid. Amount is invalid! "));
	}

	@Test
	public void testCreateTransaction_invalidType() throws Exception {
		Client client1 = new Client();
		client1.setClientId(100L);
		client1.setCnp("1940407110011");
		client1.setName("Adrian Pena");
//		client1.setIban("RO19RZBR8637876275195736");
		client1.setWallet("0721306901");

		Client client2 = new Client();
		client2.setClientId(101L);
		client2.setCnp("1940407110022");
		client2.setName("Ion Ionescu");
//		client2.setIban("RO19RZBR8637876275195737");
		client2.setWallet("0721306902");

		Transaction transaction = new Transaction();
		transaction.setAmount("100");
		transaction.setDescription("Datorie");
		transaction.setClientFrom(client1);
		transaction.setClientTo(client2);
		transaction.setType(TransactionType.IBAN_TO_IBAN);
		transaction.setTransactionId(100L);

		ObjectMapper mapper = new ObjectMapper();
		String transactionString = mapper.writeValueAsString(transaction);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON)
						.content(transactionString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value(
						"Transaction is invalid. There's not enough information for the clients available for this transaction!"));
	}

	@Test
	public void testCreateTransaction_invalidDescription() throws Exception {
		Client client1 = new Client();
		client1.setClientId(100L);
		client1.setCnp("1940407110011");
		client1.setName("Adrian Pena");
		client1.setIban("RO19RZBR8637876275195736");
		client1.setWallet("0721306901");

		Client client2 = new Client();
		client2.setClientId(101L);
		client2.setCnp("1940407110022");
		client2.setName("Ion Ionescu");
		client2.setIban("RO19RZBR8637876275195737");
		client2.setWallet("0721306902");

		Transaction transaction = new Transaction();
		transaction.setAmount("100");
		transaction.setDescription("Datorie_invalida");
		transaction.setClientFrom(client1);
		transaction.setClientTo(client2);
		transaction.setType(TransactionType.IBAN_TO_IBAN);
		transaction.setTransactionId(100L);

		ObjectMapper mapper = new ObjectMapper();
		String transactionString = mapper.writeValueAsString(transaction);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON)
						.content(transactionString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("$.error")
						.value("Transaction is invalid. Description is invalid! "));
	}

}
