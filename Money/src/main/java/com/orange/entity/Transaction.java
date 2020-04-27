package com.orange.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity(name = "transactions")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "transaction_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TransactionType type;

	@Column(name = "transaction_description")
	private String description;

	@Column(name = "transaction_amount")
	private String amount;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "transaction_from")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Client clientFrom;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "transaction_to")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Client clientTo;

	public enum TransactionType {
		IBAN_TO_IBAN, IBAN_TO_WALLET, WALLET_TO_IBAN, WALLET_TO_WALLET
	}

	public Transaction() {
		super();
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public Client getClientFrom() {
		return clientFrom;
	}

	public void setClientFrom(Client clientFrom) {
		this.clientFrom = clientFrom;
	}

	public Client getClientTo() {
		return clientTo;
	}

	public void setClientTo(Client clientTo) {
		this.clientTo = clientTo;
	}

	@Override
	public String toString() {
		return "Transaction [description=" + description + ", amount=" + amount + "]";
	}

}
