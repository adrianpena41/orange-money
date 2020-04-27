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
import com.orange.repository.ClientRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ClientController.class, Receiver.class })
public class ClientControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private ClientRepository clientRepository;

	Client client;

	@Before
	public void onLoad() {
		client = new Client();
		client.setClientId(100L);
		client.setCnp("1940407110011");
		client.setName("Adrian Pena");
		client.setIban("RO19RZBR8637876275195736");
		client.setWallet("0721306901");
	}

	@Test
	public void contextLoads() throws Exception {
		Mockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get("/api/v1/clients").accept(MediaType.APPLICATION_JSON)).andReturn();
		System.out.println(mvcResult.getResponse());
		Mockito.verify(clientRepository).findAll();
	}

	@Test
	public void testGetClient() throws Exception {
		List<Client> clientList = Arrays.asList(client);
		given(clientRepository.findAll()).willReturn(clientList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/clients").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$..clientId").value(100));
	}

	@Test
	public void testGetClientById() throws Exception {
		Optional<Client> optionalClient = Optional.of(client);
		given(clientRepository.findById(client.getClientId())).willReturn(optionalClient);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/api/v1/clients/{clientId}", 100L)
						.accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.clientId").value(100));
	}

	@Test
	public void testPostClient() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String clientString = mapper.writeValueAsString(client);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON)
						.content(clientString).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

}
