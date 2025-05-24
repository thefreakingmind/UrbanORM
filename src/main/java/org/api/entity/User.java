package org.api.entity;

import org.api.model.Column;
import org.api.model.Table;

@Table(name = "users")
public class User {

  @Column(name = "id", primaryKey = true, autoIncrement = true)
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "email")
  private String email;



  public User() {
  }

  public User(String name, String email) {
    this.name = name;
    this.email = email;
  }

  // Getters and Setters
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
  }
}
