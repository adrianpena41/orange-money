package com.orange.controller;

import java.net.ConnectException;
import java.util.regex.Pattern;

import javax.jms.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.orange.config.AppConstants;
import com.orange.entity.Client;

import io.micrometer.core.instrument.util.StringUtils;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

	@Value("${orange.money-url}")
	private String MONEY_URL;
	private final String MONEY_CLIENT_PATH = "/api/v1/clients";

	@Autowired
	private Queue queueClients;
	@Autowired
	private JmsTemplate jmsTemplate;

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get() {
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.getForObject(MONEY_URL + MONEY_CLIENT_PATH, String.class);
		return ResponseEntity.ok(response);
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(method = RequestMethod.GET, path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get(@PathVariable Long id) {
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.getForObject(MONEY_URL + MONEY_CLIENT_PATH + "/" + id, String.class);
		return ResponseEntity.ok(response);
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> create(@RequestBody final Client client) throws JSONException {
		String validationErrors = validateClient(client);
		if (StringUtils.isBlank(validationErrors)) {

			try {
				jmsTemplate.convertAndSend(queueClients, client);
			} catch (JmsException e) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
						.body(new JSONObject()
								.put("response",
										"Client has been validated, but the Client could not be created at this time!")
								.toString());
			}
			return ResponseEntity
					.ok(new JSONObject().put("response", "Client has been validated and will be created!").toString());
		} else {
			return ResponseEntity.badRequest()
					.body(new JSONObject().put("error", "Client is invalid. " + validationErrors).toString());
		}
	}

	private String validateClient(Client client) {
		StringBuilder validationErrors = new StringBuilder();
		validationErrors.append(validateField(client.getCnp(), AppConstants.CNP_REGEX, "Cnp is invalid!"));
		validationErrors.append(validateField(client.getName(), AppConstants.NAME_REGEX, "Name is invalid!"));
		String iban = client.getIban();
		String wallet = client.getWallet();
		if (StringUtils.isNotBlank(iban) || StringUtils.isNotBlank(wallet)) {
			if (StringUtils.isNotBlank(iban)) {
				validationErrors
						.append(validateField(iban.replace(" ", ""), AppConstants.IBAN_REGEX, "Iban is invalid!"));
			}
			// validarea campului wallet se face ca la numar de telefon
			if (StringUtils.isNotBlank(wallet)) {
				validationErrors.append(validateField(wallet, AppConstants.WALLET_REGEX, "Wallet is invalid!"));
			}
		} else {
			validationErrors.append("Both Iban and Wallet are blank, at least one of them is mandatory!");
		}
		return validationErrors.toString();
	}

	private String validateField(String field, String regex, String errorMessage) {
		return Pattern.matches(regex, field) ? "" : errorMessage;
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete(MONEY_URL + MONEY_CLIENT_PATH + "/" + id);
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(value = "{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> update(@PathVariable Long id, @RequestBody final Client client) throws JSONException {
		RestTemplate restTemplate = new RestTemplate();
		String validationErrors = validateClient(client);
		if (StringUtils.isBlank(validationErrors)) {
			restTemplate.put(MONEY_URL + MONEY_CLIENT_PATH, client);
			return ResponseEntity.ok("");
		} else {
			return ResponseEntity.badRequest()
					.body(new JSONObject().put("error", "Client is invalid. " + validationErrors).toString());
		}
	}

	@Recover
	public ResponseEntity<String> recover(RestClientException e) {
		System.out.println("Aplicatia nu este disponibila momentan, va rugam incercati mai tarziu");
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Aplicatia nu este disponibila momentan, va rugam incercati mai tarziu");
	}

	@Recover
	public ResponseEntity<String> recover(ConnectException e) {
		System.out.println("Aplicatia nu este disponibila momentan, va rugam incercati mai tarziu");
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Aplicatia nu este disponibila momentan, va rugam incercati mai tarziu");
	}
}
