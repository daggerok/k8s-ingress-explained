package com.example.k8snginxingressexample.hello.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class HelloResponse {
    String key;
    String value;
}
