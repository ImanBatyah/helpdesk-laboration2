
# Helpdesk – Laboration 2

## Beskrivning
Ett webbaserat ärendehanteringssystem utvecklat med Java, Spring Boot, Spring Data JPA, Spring Security, Thymeleaf och MySQL.

Projektet är utvecklat inom kursen Backendutveckling och systemintegration.

## Funktioner
- Inloggning med Spring Security
- Lösenordshantering med BCrypt
- Skapa och visa supportärenden
- Hantera ärendestatus och prioritet
- Kommentera supportärenden
- Tilldela ärenden till supportmedarbetare
- Rollbaserad behörighetskontroll

## Användarroller
- USER – skapar och följer sina supportärenden
- AGENT – arbetar med tilldelade supportärenden
- ADMIN – hanterar supportärenden och användarkonton

## VG – Administratörsfunktioner
Administratören kan:
- Skapa nya användare
- Ändra användarroller
- Aktivera användarkonton
- Inaktivera användarkonton

## Tekniker
- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- Hibernate
- Thymeleaf
- MySQL
- Maven

## Starta applikationen
1. Installera Java 21 och MySQL.
2. Skapa databasen `helpdesk_db`.
3. Konfigurera miljövariablerna:
   - DB_USERNAME
   - DB_PASSWORD
   - HELPDESK_ADMIN_EMAIL
   - HELPDESK_ADMIN_PASSWORD
   - HELPDESK_AGENT_EMAIL
   - HELPDESK_AGENT_PASSWORD
4. Starta applikationen med:

   ./mvnw spring-boot:run

5. Öppna http://localhost:8080

## Testning
Projektet har byggts och verifierats med Maven:

./mvnw clean verify
