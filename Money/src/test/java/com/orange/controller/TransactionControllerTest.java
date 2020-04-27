package com.orange.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.config.Receiver;
import com.orange.entity.Client;
import com.orange.entity.Transaction;
import com.orange.entity.Transaction.TransactionType;
import com.orange.repository.TransactionRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { TransactionController.class, Receiver.class })
public class TransactionControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private TransactionRepository transactionRepository;

	Transaction transaction;

	@Before
	public void onLoad() {
		transaction = new Transaction();
		Client client1 = new Client();
		client1 = new Client();
		client1.setClientId(100L);
		client1.setCnp("1940407110011");
		client1.setName("Adrian Pena");
		client1.setIban("RO19RZBR8637876275195736");
		client1.setWallet("0721306901");

		Client client2 = new Client();
		client2 = new Client();
		client2.setClientId(101L);
		client2.setCnp("1940407220022");
		client2.setName("Ion Ionescu");
		client2.setIban("RO19RZBR8637876275195736");
		client2.setWallet("0733444555");

		transaction.setClientFrom(client1);
		transaction.setClientTo(client2);
		transaction.setAmount("1000");
		transaction.setDescription("Transfer");
		transaction.setType(TransactionType.IBAN_TO_IBAN);
		transaction.setTransactionId(100L);
	}

	@Test
	public void contextLoads() throws Exception {
		Mockito.when(transactionRepository.findAll()).thenReturn(Collections.emptyList());
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get("/api/v1/transactions").accept(MediaType.APPLICATION_JSON))
				.andReturn();
		System.out.println(mvcResult.getResponse());
		Mockito.verify(transactionRepository).findAll();
	}

	@Test
	public void testGetTransaction() throws Exception {
		List<Transaction> TransactionList = Arrays.asList(transaction);
		given(transactionRepository.findAll()).willReturn(TransactionList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$..transactionId").value(100));
	}

	@Test
	public void testGetTransactionById() throws Exception {
		Optional<Transaction> optionalTransaction = Optional.of(transaction);
		given(transactionRepository.findById(transaction.getTransactionId())).willReturn(optionalTransaction);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/api/v1/transactions/{transactionId}", 100L)
						.accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.transactionId").value(100));
	}

	@Test
	public void testPostTransaction() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String transactionString = mapper.writeValueAsString(transaction);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON)
						.content(transactionString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testGetRaport() throws Exception {
		List<Transaction> TransactionList = Arrays.asList(transaction);
		given(transactionRepository.findAll()).willReturn(TransactionList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions/raport")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("Nume: Ion Ionescu\n" + "\n" + "CNP: 1940407220022\n" + "\n"
								+ "IBAN: RO19RZBR8637876275195736\n" + "\n" + "Tranzactii:\n" + "\n"
								+ "	1. IBAN_TO_IBAN | 1 tranzactie | 1000 (+) | 0 (-)\n"
								+ "		a. Transaction [description=Transfer, amount=1000] (+)\n"
								+ "	2. IBAN_TO_WALLET | fara tranzactii\n" + "	3. WALLET_TO_IBAN | fara tranzactii\n"
								+ "	4. WALLET_TO_WALLET | fara tranzactii\n"
								+ "----------------------------------------------------------\n" + "Nume: Adrian Pena\n"
								+ "\n" + "CNP: 1940407110011\n" + "\n" + "IBAN: RO19RZBR8637876275195736\n" + "\n"
								+ "Tranzactii:\n" + "\n" + "	1. IBAN_TO_IBAN | 1 tranzactie | 0 (+) | 1000 (-)\n"
								+ "		a. Transaction [description=Transfer, amount=1000] (-)\n"
								+ "	2. IBAN_TO_WALLET | fara tranzactii\n" + "	3. WALLET_TO_IBAN | fara tranzactii\n"
								+ "	4. WALLET_TO_WALLET | fara tranzactii\n"));
	}

	@Test
	public void testGetRaportByCnp() throws Exception {
		List<Transaction> TransactionList = Arrays.asList(transaction);
		given(transactionRepository.findAll()).willReturn(TransactionList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions/raport/1940407220022")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("Nume: Ion Ionescu\n" + "\n" + "CNP: 1940407220022\n" + "\n"
								+ "IBAN: RO19RZBR8637876275195736\n" + "\n" + "Tranzactii:\n" + "\n"
								+ "	1. IBAN_TO_IBAN | 1 tranzactie | 1000 (+) | 0 (-)\n"
								+ "		a. Transaction [description=Transfer, amount=1000] (+)\n"
								+ "	2. IBAN_TO_WALLET | fara tranzactii\n" + "	3. WALLET_TO_IBAN | fara tranzactii\n"
								+ "	4. WALLET_TO_WALLET | fara tranzactii\n"));
	}

}
