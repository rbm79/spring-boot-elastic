package com.rbm.logs.examples.demo.controller;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RestController
public class HelloController {

    public enum Department {
        HR, IT, FINANCE, MARKETING, OPERATIONS
    }

    public void performOperation(Department department, String operation) {
        
        MDC.put("custom.department", department.name());
        try {
            log.info("Starting operation: {}", operation);
            // Simula algum processamento
            Thread.sleep(1000);
            log.info("Operation completed successfully");
        } catch (InterruptedException e) {
            log.error("Error during operation", e);
        } finally {
            MDC.remove("custom.department");
        }
    }
    

    @GetMapping("/")
    @ResponseBody
    public String hello() {

        Transaction transaction = ElasticApm.currentTransaction();
        transaction.setLabel("custom-label", "HelloController");

        log.info("This is an info log");
        log.warn("This is a warning log");
        log.error("This is an error log");
        log.info("This is a log 1 with a CPF that should be masked - CPF - 279.152.799-94");
        log.info("This is a log 2 Paulo Roberto Cardoso - 27915279994");
        log.info("This is a log 3 Paulo Roberto Cardoso - RG 22.588.502-X");
        log.info("This is a log 4 Paulo Roberto Cardoso - paulo.cardoso@outlook.com");
        log.info("This is a log 5 Paulo Roberto Cardoso - 11988895677");
        log.info("This is a log 6 Paulo Roberto Cardoso - 11-98889-5677");
        log.info("This is a log 7 Paulo Roberto Cardoso - 5511988895677");
        log.info("This is a log 8: User with CPF 123.456.789-00 logged in successfully.");
        log.info("This is a log 9: Document verification: RG 12.345.678-9 validated.");
        log.error("This is a log 10: Failed transaction for card 4111-1111-1111-1111 due to insufficient funds.");
        log.info("This is a log 11: New user registered: John Doe, DOB: 15/03/1985");
        log.warn("This is a log 12: Suspicious login attempt from IP 192.168.1.100");
        log.info("This is a log 13: Travel document checked: Passport AB123456 is valid.");
        log.info("This is a log 14: Vehicle ABC-1234 entered parking lot.");
        log.info("This is a log 15: Transfer initiated from account 12345-6 to 54321-0");
        log.info("This is a log 16: PIX transaction completed using key 123e4567-e89b-12d3-a456-426614174000");
        log.debug("This is a log 17: User location updated: -23.550520, -46.633309");
        log.info("This is a log 18: Support ticket opened: Protocol 12345.678901/2023-45");
        log.debug("This is a log 19: API request with token: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        log.info("This is a log 20: New employee registration: Maria Luísa Santos Oliveira, ID: 98765, Department: IT");
        log.info("This is a log 21: New employee registration: Paulo Roberto Cardoso, email: pcardoso@hotmail.com, cpf: 234.456.223-32, rg: 22.566.766, fone: 11-98889-1756 ");

        //this field will not be indexed in elastic search
        String order_id = "87699"; 
        log.info("This is my orderid - "+order_id);

        
        performOperation(Department.HR, "Annual Review");
        performOperation(Department.IT, "System Upgrade");
        performOperation(Department.FINANCE, "Budget Planning");

        
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
