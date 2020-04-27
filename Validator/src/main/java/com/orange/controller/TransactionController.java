package com.orange.controller;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
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
import com.orange.entity.Transaction;
import com.orange.entity.Transaction.TransactionType;

import io.micrometer.core.instrument.util.StringUtils;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

	@Value("${orange.money-url}")
	private String MONEY_URL;
	private final String MONEY_TRANSACTION_PATH = "/api/v1/transactions";

	@Autowired
	private Queue queueTransactions;
	@Autowired
	private JmsTemplate jmsTemplate;

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get() {
		RestTemplate restTemplate = new RestTemplate();
		return ResponseEntity.ok(restTemplate.getForObject(MONEY_URL + MONEY_TRANSACTION_PATH, String.class));
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(method = RequestMethod.GET, path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get(@PathVariable Long id) {
		RestTemplate restTemplate = new RestTemplate();
		return ResponseEntity
				.ok(restTemplate.getForObject(MONEY_URL + MONEY_TRANSACTION_PATH + "/" + id, String.class));
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> create(@RequestBody final Transaction transaction) throws JSONException {
		String validationErrors = validateTransaction(transaction);
		if (StringUtils.isBlank(validationErrors)) {

			try {
				jmsTemplate.convertAndSend(queueTransactions, transaction);
			} catch (JmsException e) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
						.body("Transaction has been validated, but transaction can not be created at this time!");
			}
			return ResponseEntity.ok("Transaction has been validated and will be created!");
		} else {
			return ResponseEntity.badRequest()
					.body(new JSONObject().put("error", "Transaction is invalid. " + validationErrors).toString());
		}
	}

	private String validateTransaction(Transaction transaction) {
		StringBuilder validationErrors = new StringBuilder();
		TransactionType transactionType = transaction.getType();
		String amount = transaction.getAmount();
		String description = transaction.getDescription();
		validationErrors.append(validateField(amount, AppConstants.AMOUNT_REGEX, "Amount is invalid! "));
		validationErrors.append(validateField(description, AppConstants.DESCRIPTION_REGEX, "Description is invalid! "));

		Client clientFrom = transaction.getClientFrom();
		Client clientTo = transaction.getClientTo();
		if (clientFrom != null && clientTo != null && transactionType != null) {
			validationErrors.append(validateTransactionType(transactionType, clientFrom, clientTo));
		} else {
			if (clientFrom == null) {
				validationErrors.append("ClientFrom is null! ");
			}
			if (clientTo == null) {
				validationErrors.append("ClientFrom is null! ");
			}
			if (transactionType == null) {
				validationErrors.append("TransactionType is null! ");
			}
		}
		return validationErrors.toString();
	}

	private String validateTransactionType(TransactionType transactionType, Client clientFrom, Client clientTo) {
		String errorMessage = "There's not enough information for the clients available for this transaction!";
		switch (transactionType) {
		case IBAN_TO_IBAN:
			return (clientFrom.getIban() != null && clientTo.getIban() != null) ? "" : errorMessage;
		case IBAN_TO_WALLET:
			return (clientFrom.getIban() != null && clientTo.getWallet() != null) ? "" : errorMessage;
		case WALLET_TO_IBAN:
			return (clientFrom.getWallet() != null && clientTo.getIban() != null) ? "" : errorMessage;
		case WALLET_TO_WALLET:
			return (clientFrom.getWallet() != null && clientTo.getWallet() != null) ? "" : errorMessage;
		}
		return "";
	}

	private String validateField(String field, String regex, String errorMessage) {
		return Pattern.matches(regex, field) ? "" : errorMessage;
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) throws RestClientException, URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete(new URI(MONEY_URL + MONEY_TRANSACTION_PATH + "/" + id));
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(value = "{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> update(@PathVariable Long id, @RequestBody final Transaction transaction)
			throws JSONException {
		RestTemplate restTemplate = new RestTemplate();
		String validationErrors = validateTransactionType(transaction.getType(), transaction.getClientFrom(),
				transaction.getClientTo());
		if (StringUtils.isBlank(validationErrors)) {
			restTemplate.put(MONEY_URL + MONEY_TRANSACTION_PATH, transaction);
			return ResponseEntity.ok("");
		} else {
			return ResponseEntity.badRequest()
					.body(new JSONObject().put("error", "Client is invalid. " + validationErrors).toString());
		}
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(path = "/raport", method = RequestMethod.GET)
	public ResponseEntity<String> getRaport() {
		RestTemplate restTemplate = new RestTemplate();
		System.out.println("Getting raport");
		return ResponseEntity
				.ok(restTemplate.getForObject(MONEY_URL + MONEY_TRANSACTION_PATH + "/raport", String.class));
	}

	@Retryable(maxAttempts = 3, value = { RestClientException.class,
			ConnectException.class }, backoff = @Backoff(delay = 2000))
	@RequestMapping(path = "/raport/{cnpParam}", method = RequestMethod.GET)
	public ResponseEntity<String> getRaportByCnp(@PathVariable String cnpParam) {
		RestTemplate restTemplate = new RestTemplate();
		return ResponseEntity.ok(
				restTemplate.getForObject(MONEY_URL + MONEY_TRANSACTION_PATH + "/raport/" + cnpParam, String.class));
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
