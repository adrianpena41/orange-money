package com.orange.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name = "clients")
public class Client {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "client_cnp", nullable = false)
	@NotNull(message = "Please provide cnp")
	private String cnp;

	@Column(name = "client_name", nullable = false)
	@NotNull(message = "Please provide a name")
	private String name;

	@Column(name = "client_iban")
	private String iban;

	@Column(name = "client_wallet")
	private String wallet;

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getCnp() {
		return cnp;
	}

	public void setCnp(String cnp) {
		this.cnp = cnp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public String getWallet() {
		return wallet;
	}

	public void setWallet(String wallet) {
		this.wallet = wallet;
	}

	public Client() {
		super();
	}

	@Override
	public String toString() {
		return "Client [cnp=" + cnp + ", name=" + name + ", iban=" + iban + ", wallet=" + wallet + "]";
	}

}
