package com.orange.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.entity.Client;
import com.orange.repository.ClientRepository;

@RestController
@RequestMapping("/api/v1/clients")
@Configuration
public class ClientController {

	@Autowired
	private ClientRepository clientRepository;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Client> get() {
		return clientRepository.findAll();
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get(@PathVariable Long id) throws JsonProcessingException, JSONException {
		Optional<Client> optionalClient = clientRepository.findById(id);
		if (optionalClient.isPresent()) {
			ObjectMapper mapper = new ObjectMapper();
			return ResponseEntity.ok(mapper.writeValueAsString(optionalClient.get()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("response", "Client with id " + id + " not found.").toString());
		}
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Client create(@RequestBody final Client client) {
		return clientRepository.saveAndFlush(client);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) {
		clientRepository.deleteById(id);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	public Client update(@PathVariable Long id, @RequestBody final Client client) {
		Client existingClient = clientRepository.findById(id).get();
		BeanUtils.copyProperties(client, existingClient, "clientId");
		return clientRepository.saveAndFlush(existingClient);
	}

	@JmsListener(destination = "orange-money-clients")
	public void processMessage(String message) {
		System.out.println("Message received: " + message);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Client client = mapper.readValue(message, Client.class);
			clientRepository.saveAndFlush(client);
			System.out.println("Client created: " + client.toString());
		} catch (JsonMappingException e) {
			System.out.println("JsonMappingException " + e.getMessage());
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			System.out.println("JsonProcessingException " + e.getMessage());
			e.printStackTrace();
		}
	}
}
