package org.api;

import org.api.entity.User;
import org.api.service.QueryExcutionManager;

import java.util.List;


public class Main {
  public static void main(String[] args) {
    System.out.println("=== Basic CRUD Tests ===");
    QueryExcutionManager.createTable(User.class);
    User user = new User("Salman", "salmansiddiqui100@gmail.com");
    QueryExcutionManager.insert(user);
    List<User> users = QueryExcutionManager.findAll(User.class);
    System.out.println("Users in database:");
    users.forEach(System.out::println);

    User newUser = QueryExcutionManager.findById(User.class, 1);
    if (newUser != null) {
      newUser.setName("SalmanS");
      QueryExcutionManager.update(newUser);
    }
    User updatedUser = QueryExcutionManager.findById(User.class, 1);
    System.out.println("Updated user: " + updatedUser);



    System.out.println("\n=== Dual Write Mode Test ===");
    QueryExcutionManager.setDualWriteEnabled(true);
    QueryExcutionManager.createTable(User.class);
    User dualWriteUser = new User("John Doe", "salmaniscool@gmail.com");
    QueryExcutionManager.insert(dualWriteUser);

    User fetchedDualWriteUser = QueryExcutionManager.findById(User.class, 1);
    System.out.println("Fetched User in Dual Write Mode: " + fetchedDualWriteUser);
  }

}