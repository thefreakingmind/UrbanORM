# Urban ORM
This project demonstrates basic CRUD (Create, Read, Update, Delete) operations using an
in-memory ORM implementation in Java.
It supports dual-write functionality, allowing data to be written to two databases simultaneously.
The code can easily be integrated into a Spring Boot application by adding the necessary
dependencies.
Requirements
1. JDK 8 or higher: The code is written using Java 8+ features.
2. MySQL Databases:
- Two databases are required for dual-write functionality:
- urbanDB (Primary Database)
- urbanDBSlave (Secondary Database)
3. Maven: The project uses Maven for dependency management and building the project.
   Steps to Run the Code
### 1. Set Up MySQL Databases
First, create two databases in MySQL by running the following SQL commands:
```sql
CREATE DATABASE urbanDB;
CREATE DATABASE urbanDBSlave;
```
### 2. Change the DB Configuration
Change the database credentials in the dbconfig.yaml file located in the src/main/resources folder.
Example dbconfig.yaml file:
```yaml
database:
primary:
url: "jdbc:mysql://localhost:3306/urbanDB"
username: "root"
password: "Mpasas1109123@"
secondary:
url: "jdbc:mysql://localhost:3306/urbanDBSlave"
username: "root"
password: "Mpasas1109123@"
```
Make sure to replace the username and password fields with your actual database credentials.
### 3. Execute the ORM
- Open the Main class in the src/main/java/org/api/Main.java file.
- The Main.java file demonstrates basic CRUD operations, creating a User table, and
  inserting/updating records.
- You can run the Main.java class, which will execute the ORM logic:
- Create a table `User`
- Insert a new user record
- Fetch and update an existing record
- Test dual-write functionality by writing to both databases simultaneously.
### 4. Integrate with a Spring Boot Project
The ORM library is designed to be easily integrated into any Spring Boot project.
To use it in your own Spring Boot project:
1. Run the following Maven command to build the project and generate a .jar file:
```bash
mvn clean install
```
2. Add the generated .jar file to your Spring Boot project as a dependency by including it in the
   pom.xml:
```xml
<dependency>
<groupId>your.group.id</groupId>
<artifactId>urban-orm</artifactId>
<version>1.0-SNAPSHOT</version> 
<scope>system</scope>
<systemPath>${project.basedir}/path/to/urban-orm.jar</systemPath>
</dependency>
```
Alternatively, if hosted in a repository (like Maven Central or a private repository), you can
reference it directly.
3. In your Spring Boot project, you can create new tables by simply adding the @Table annotation to
   any POJO class and using the @Column annotation for each field.
   Example:
```java
@Table(name = "user")
public class User {
@Column(name = "id")
private int id;
@Column(name = "name")
private String name;
@Column(name = "email")
private String email;
}
```
The ORM will automatically map this POJO to a table named user in the configured database, just
like JPA/Hibernate would.
- Dual Write Mode: Data can be written to both primary and secondary databases.
- CRUD Operations: Supports basic Create, Read, Update, Delete operations.
- Spring Boot Integration: The ORM can be easily integrated into any Spring Boot project.
