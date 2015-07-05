package org.javiermoreno.torrentscratcher;

import ch.qos.logback.classic.BasicConfigurator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TorrentScratcherApplication   {

    @Bean
    public CommandLineRunner getRunner() {
        return new Runner();
    }
    
    public static void main(String[] args) {
        BasicConfigurator.configureDefaultContext();
        SpringApplication.run(TorrentScratcherApplication.class, args);
    }

}
