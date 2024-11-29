package org.example.chatgptconnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/chat")
public class ChatGPTController {

    @Autowired
    private ChatGPTService chatGPTService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody String userMessage) {
        try {
            Thread.sleep(2000);
            return chatGPTService.sendMessage(userMessage);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode()== HttpStatus.TOO_MANY_REQUESTS) {
                return "Too many requests\n" + e.getMessage();
            } else {
                return e.getResponseBodyAsString();
            }
        }
        catch (Exception e) {
            return e.getMessage();
        }

    }
}
