package com.anderscore.goldschmiede.springbatch.samples.infra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
// tag::code[]
@Data
public class Person {
    private String firstName;
    private String lastName;
}
// end::code[]