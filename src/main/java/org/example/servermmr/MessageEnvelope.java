package org.example.servermmr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEnvelope implements Serializable {
    private String type;     // response, broadcast
    private String command;  // JOIN, MOVE и т.п.
    private String status;   // OK, ERROR
    private String message;  // человекочитаемое сообщение
    private String token;    // токен (если есть)
    private Map<String, String> payload = new HashMap<>(); // доп. данные
}