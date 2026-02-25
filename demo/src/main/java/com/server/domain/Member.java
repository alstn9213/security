package com.server.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter 
@Setter
public class Member extends BaseTimeEntity {
  
  @Id
  private String id; // Username 역할

  private String password;

  private String name;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private List<Role> roles = new ArrayList<>();

  public void update(String password, String name) {
      this.password = password;
      this.name = name;
  }

}
