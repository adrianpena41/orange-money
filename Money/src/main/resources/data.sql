DROP TABLE IF EXISTS TRANSACTIONS cascade;
DROP TABLE IF EXISTS CLIENTS cascade;
 
CREATE TABLE CLIENTS (
  client_id INT AUTO_INCREMENT  PRIMARY KEY,
  client_cnp VARCHAR(250) NOT NULL,
  client_name VARCHAR(250) NOT NULL,
  client_iban VARCHAR(250),
  client_wallet VARCHAR(250)
);


CREATE TABLE TRANSACTIONS (
  transaction_id INT AUTO_INCREMENT  PRIMARY KEY,
  transaction_type VARCHAR(250) NOT NULL,
  transaction_from INT NOT NULL,
  transaction_to INT NOT NULL,
  transaction_description VARCHAR(250) NOT NULL,
  transaction_amount VARCHAR(250) NOT NULL,
  FOREIGN KEY (transaction_from) REFERENCES Clients(client_id),
  FOREIGN KEY (transaction_to) REFERENCES Clients(client_id)
);


INSERT INTO clients (client_id, client_cnp, client_name, client_iban, client_wallet) VALUES
  (1, '1940407420025', 'Adrian Pena', 'RO19BCRA8637876275195736', '0721306901'),
  (2, '3930608420025', 'Gheorghe Popescu', 'RO19RZBR8637876275195736', '0711000223'),
  (3, '1960702070713', 'ION Ionescu', 'RO19RZBR8637876275195736', '0723999000'),
  (4, '2960702070713', 'Ioana Ionescu', 'RO19RZBR8637876275195736', '0711222333');

INSERT INTO TRANSACTIONS (transaction_type, transaction_from, transaction_to, transaction_description, transaction_amount) VALUES
  ('IBAN_TO_IBAN', 1, 2, 'Marfa', '250'),
  ('IBAN_TO_IBAN', 1, 2, 'Marfa', '270'),
  ('IBAN_TO_WALLET', 1, 2, 'Marfa', '100'),
  ('WALLET_TO_IBAN', 1, 2, 'Marfa', '200'),
  ('IBAN_TO_IBAN', 3, 4, 'Datorie', '1000'),
  ('IBAN_TO_IBAN', 3, 4, 'Imprumut', '2000'),
  ('IBAN_TO_IBAN', 3, 4, 'Cadou', '3000'),
  ('WALLET_TO_IBAN', 3, 4, 'Desc1', '240'),
  ('WALLET_TO_IBAN', 3, 4, 'Desc2', '240'),
  ('WALLET_TO_IBAN', 3, 4, 'Desc3', '240'),
  ('WALLET_TO_IBAN', 3, 4, 'Desc4', '240'),
  ('WALLET_TO_IBAN', 3, 4, 'Desc5', '240');

