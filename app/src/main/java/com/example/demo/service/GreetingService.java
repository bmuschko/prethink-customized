package com.example.demo.service;

import com.example.demo.model.Greeting;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class GreetingService {

    private final AtomicLong counter = new AtomicLong();

    public Greeting createGreeting(String name) {
        return new Greeting(counter.incrementAndGet(), "Hello, " + name + "!");
    }
}
