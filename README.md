
# Campus Lost & Found Portal

A Java-based desktop application that helps students report lost and found items and recover their belongings through a claim management system.

## Features

### Student Module
- Student registration and login
- Secure password hashing
- Report lost items
- Report found items
- View all reported items
- Submit item claims
- Track claim status

### Admin Module
- View registered students
- Manage lost and found reports
- Update item details
- Delete item records
- Approve or reject claims

## Technologies Used
- Java 21
- Java Swing
- JDBC
- SQLite Database
- Apache Maven
- SQL

## Database Tables
- Users
- Items
- Claims

## How to Run

1. Install JDK 21 and Apache Maven.
2. Open the project folder in VS Code.
3. Open the terminal.
4. Run:

```bash
mvn clean compile
```

5. Start the application:

```bash
mvn "-Dexec.mainClass=com.campus.Main" exec:java
```

## Security
Passwords are stored as salted PBKDF2 hashes instead of plain text.

The default administrator account is for demonstration only. Change its credentials before real deployment.

## Future Improvements
- Item image uploads
- Advanced search and filters
- Smart item matching
- Email notifications

## Project Type
Java JDBC and Database Management Project.
