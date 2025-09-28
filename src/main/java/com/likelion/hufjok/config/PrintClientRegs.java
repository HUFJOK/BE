package com.likelion.hufjok.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class PrintClientRegs implements CommandLineRunner {

    private final ClientRegistrationRepository repo;

    public PrintClientRegs(ClientRegistrationRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo instanceof InMemoryClientRegistrationRepository r) {
            r.forEach(reg ->
                    System.out.println(">> OIDC client loaded: " + reg.getRegistrationId())
            );
        }
    }
}
