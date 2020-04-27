# orange-money
Pentru acest proiect am folosit Java 8, SpringBoot 2, H2 DB in memory, ActiveMQ 5, JUnit/Mockito.

Am creat microserviciul Money si microserviciul Validator. 
Money este aplicatia de baza care se ocupa de layer-ul de persistenta in baza de date.
Validator este aplicatia care valideaza tranzactiile inainte de a fi trimise la aplicatia Money.

Structura bazei de date folosita este descrisa in data.sql. Am creat entitatea CLIENT si entitatea TRANSACTION, fiecare tranzactie avand 2 clienti.
Raportul generat nu este 100% ca cel din exemplu, am agregat tranzactiile primite si separat tranzactiile trimise pentru fiecare client. Atat langa suma tranzanctiilor cat si la finalul detalierei fiecarei tranzactii am adaugat (+) sau (-) pentru a putea diferentia tranzactiile.
