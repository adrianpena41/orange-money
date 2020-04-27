package com.orange.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.entity.Client;
import com.orange.entity.Transaction;
import com.orange.entity.Transaction.TransactionType;
import com.orange.repository.TransactionRepository;

@RestController
@RequestMapping("/api/v1/transactions")
@Configuration
public class TransactionController {
	private static final String TAB = "\t";
	private static final String EOL = "\n";
	@Autowired
	private TransactionRepository transactionRepository;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Transaction> get() {
		return transactionRepository.findAll();
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get(@PathVariable Long id) throws JsonProcessingException, JSONException {
		Optional<Transaction> optionalTransaction = transactionRepository.findById(id);
		if (optionalTransaction.isPresent()) {
			ObjectMapper mapper = new ObjectMapper();
			return ResponseEntity.ok(mapper.writeValueAsString(optionalTransaction.get()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("response", "Transaction with id " + id + " not found.").toString());
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public Transaction create(@RequestBody final Transaction transaction) {
		return transactionRepository.saveAndFlush(transaction);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) {
		transactionRepository.deleteById(id);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	public Transaction update(@PathVariable Long id, @RequestBody final Transaction transaction) {
		Transaction existingTransaction = transactionRepository.findById(id).get();
		BeanUtils.copyProperties(transaction, existingTransaction, "transactionId");
		return transactionRepository.saveAndFlush(existingTransaction);
	}

	@RequestMapping(value = "/raport", method = RequestMethod.GET)
	public String getRaport() {
		StringBuilder sb = new StringBuilder();
		List<Transaction> allTransactions = transactionRepository.findAll();

		doRaport(sb, allTransactions, null);

		return sb.toString();
	}

	@RequestMapping(value = "/raport/{cnp}", method = RequestMethod.GET)
	public String getRaportByCnp(@PathVariable String cnp) {
		StringBuilder sb = new StringBuilder();
		List<Transaction> allTransactions = transactionRepository.findAll();

		List<Transaction> fromTransactions = allTransactions.stream()
				.filter(transaction -> transaction.getClientFrom().getCnp().equals(cnp)).collect(Collectors.toList());
		List<Transaction> toTransactions = allTransactions.stream()
				.filter(transaction -> transaction.getClientTo().getCnp().equals(cnp)).collect(Collectors.toList());
		fromTransactions.addAll(toTransactions);
		doRaport(sb, fromTransactions, cnp);
		return sb.toString();
	}

	private void doRaport(StringBuilder sb, List<Transaction> allTransactions, String cnpParam) {
		Map<Client, List<Transaction>> transactionsByClientFrom = allTransactions.stream()
				.collect(Collectors.groupingBy(Transaction::getClientFrom));
		Map<Client, List<Transaction>> transactionsByClientTo = allTransactions.stream()
				.collect(Collectors.groupingBy(Transaction::getClientTo));

		// merge map
		transactionsByClientTo.entrySet().forEach(entry -> {
			transactionsByClientFrom.merge(entry.getKey(), entry.getValue(), (list1, list2) -> {
				list1.addAll(list2);
				return list1;
			});
		});

		if (cnpParam != null) {
		}
		transactionsByClientFrom.forEach((client, transactions) -> {
			if (cnpParam != null) {
				if (client.getCnp().equals(cnpParam)) {
					appendClientToSb(sb, client, transactions);
				}
			} else {
				appendClientToSb(sb, client, transactions);
			}
		});

		// remove last report separator
		if (sb.length() > 60) {
			sb.delete(sb.length() - 60, sb.length() - 1);
		}
	}

	private void appendClientToSb(StringBuilder sb, Client client, List<Transaction> transactions) {
		sb.append("Nume: ").append(client.getName()).append(EOL).append(EOL);
		sb.append("CNP: ").append(client.getCnp()).append(EOL).append(EOL);
		sb.append("IBAN: ").append(client.getIban()).append(EOL).append(EOL);
		sb.append("Tranzactii:").append(EOL).append(EOL);

		Map<TransactionType, List<Transaction>> transactionsByType = transactions.stream()
				.collect(Collectors.groupingBy(Transaction::getType));
		TransactionType[] typeValues = Transaction.TransactionType.values();
		for (TransactionType type : typeValues) {
			List<Transaction> transactionList = transactionsByType.get(type);

			sb.append(TAB).append(type.ordinal() + 1).append(". ").append(type).append(" | ")
					.append(getTransactionCount(transactionList))
					.append(getAmountReceived(transactionList, client.getCnp()))
					.append(getAmountSent(transactionList, client.getCnp())).append(EOL);
			if (transactionList != null) {
				int[] counter = { 97 };
				transactionList.stream().forEach(transaction -> {
					String sign = transaction.getClientFrom().getCnp().equals(client.getCnp()) ? " (-)" : " (+)";
					sb.append(TAB + TAB).append((char) counter[0]).append(". ").append(transaction.toString())
							.append(sign).append(EOL);
					counter[0]++;
				});
			}
		}
		sb.append("----------------------------------------------------------" + EOL);
	}

	private String getTransactionCount(List<Transaction> transactionList) {
		String transactionCount;
		if (transactionList != null) {
			transactionCount = transactionList.size() > 1 ? transactionList.size() + " tranzactii"
					: transactionList.size() + " tranzactie";
		} else {
			transactionCount = "fara tranzactii";
		}
		return transactionCount;
	}

	private String getAmountReceived(List<Transaction> transactionList, String cnp) {
		String amountReceivedValue = "";
		if (transactionList != null) {
			Optional<Integer> amountReceived = transactionList.stream()
					.filter(t -> t.getClientTo().getCnp().equals(cnp))
					.map(transaction -> Integer.valueOf(transaction.getAmount())).reduce(Integer::sum);
			amountReceivedValue = " | " + (amountReceived.isPresent() ? amountReceived.get() : 0) + " (+)";
		}
		return amountReceivedValue;
	}

	private String getAmountSent(List<Transaction> transactionList, String cnp) {
		String amountSentValue = "";
		if (transactionList != null) {
			Optional<Integer> amountSent = transactionList.stream().filter(t -> t.getClientFrom().getCnp().equals(cnp))
					.map(transaction -> Integer.valueOf(transaction.getAmount())).reduce(Integer::sum);
			amountSentValue = " | " + (amountSent.isPresent() ? amountSent.get() : 0) + " (-)";

		}
		return amountSentValue;
	}

	@JmsListener(destination = "orange-money-transactions")
	public void listener(String message) {
		System.out.println("Message received: " + message);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Transaction transaction = mapper.readValue(message, Transaction.class);
			transactionRepository.saveAndFlush(transaction);
			System.out.println("Transaction created: " + transaction.toString());
		} catch (JsonMappingException e) {
			System.out.println("JsonMappingException " + e.getMessage());
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			System.out.println("JsonProcessingException " + e.getMessage());
			e.printStackTrace();
		}
	}
}
