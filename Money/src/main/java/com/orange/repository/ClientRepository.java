package com.orange.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orange.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

}
