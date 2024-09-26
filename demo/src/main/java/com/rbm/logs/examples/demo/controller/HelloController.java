package com.rbm.logs.examples.demo.controller;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RestController
public class HelloController {

    @GetMapping("/")
    @ResponseBody
    public String hello() {

        Transaction transaction = ElasticApm.currentTransaction();
        transaction.setLabel("custom-label", "HelloController");

        log.info("This is an info log");
        log.warn("This is a warning log");
        log.error("This is an error log");
        log.info("This is a log with a CPF that should be masked - CPF - 279.152.799-94");


        return """
               <!DOCTYPE html>
               <html lang="en">
               <head>
                   <meta charset="UTF-8">
                   <meta name="viewport" content="width=device-width, initial-scale=1.0">
                   <title>Welcome to Spring Boot</title>
                   <style>
                       body {
                           font-family: Arial, sans-serif;
                           display: flex;
                           justify-content: center;
                           align-items: center;
                           height: 100vh;
                           margin: 0;
                           background-color: #f0f0f0;
                       }
                       .container {
                           text-align: center;
                           padding: 20px;
                           border-radius: 10px;
                           background-color: white;
                           box-shadow: 0 0 10px rgba(0,0,0,0.1);
                       }
                       h1 {
                           color: #4a4a4a;
                       }
                       p {
                           color: #7a7a7a;
                       }
                   </style>
               </head>
               <body>
                   <div class="container">
                       <h1>Welcome to Spring Boot!</h1>
                       <p>This HTML page is returned by HelloController.</p>
                       <p>Current time: %s</p>
                   </div>
               </body>
               </html>
               """.formatted(java.time.LocalDateTime.now());
    }
}
