package org.api;

import org.api.entity.User;
import org.api.service.QueryExecutor;

import java.util.List;


public class Main {
  public static void main(String[] args) {
    QueryExecutor.createTable(User.class);
    User user = new User("Salman", "salmansiddiqui100@gmail.com");
    QueryExecutor.insert(user);

    List<User> users = QueryExecutor.findAll(User.class);
    System.out.println("Users in database:");
    users.forEach(System.out::println);

    User newUser = QueryExecutor.findById(User.class, 1);
    if (newUser != null) {
      newUser.setName("SalmanS");
      QueryExecutor.update(newUser);
    }

    User updatedUser = QueryExecutor.findById(User.class, 1);
    QueryExecutor.deleteById(User.class, 1);
  }

}