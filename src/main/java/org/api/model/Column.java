package org.api.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
  String name();
  boolean primaryKey() default false;
  boolean autoIncrement() default false;
}
