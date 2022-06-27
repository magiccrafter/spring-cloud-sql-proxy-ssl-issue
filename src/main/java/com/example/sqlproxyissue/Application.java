package com.example.sqlproxyissue;

import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Slf4j
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @AllArgsConstructor
    @RestController
    public class GreetingsController {

        private final PgRepository repository;

        @GetMapping("/hi")
        public Mono<String> greeting() {
            return Mono.just("hi");
        }

        @GetMapping("/books/{id}")
        public Mono<Book> getBookById(@PathVariable Integer id) {
            return repository.getOneById(id);
        }

        @GetMapping("/books/gen/{id}")
        public Mono<Book> createOneBook(@PathVariable Integer id) {
            return repository.createOne(id);
        }

        @GetMapping("/books/genN/{n}")
        public Flux<Book> gen10k(@PathVariable int n) {
            return repository.generateFirstN(n);
        }

        @GetMapping("/books")
        public Flux<Book> getAll() {
            return repository.getAll();
        }

        @GetMapping("/reset")
        public Mono<Integer> reset() {
            return repository.deleteAll();
        }
    }

    @Component
    @AllArgsConstructor
    public class PgRepository {

        private final R2dbcEntityTemplate template;

        public Flux<Book> getAll() {
            return template.select(Query.empty(), Book.class);
        }

        public Mono<Book> getOneById(int id) {
            return template.selectOne(query(where("id").is(id)), Book.class);
        }

        public Mono<Book> createOne(int id) {
            return template.insert(new Book(id, "Name" + id));
        }

        public Mono<Integer> deleteAll() {
            return template.delete(Book.class).all();
        }

        public Flux<Book> generateFirstN(int n) {
            return Flux.range(1, n)
                .map(id -> new Book(id, "Name" + id))
                .flatMap(template::insert)
                .doOnNext(i -> log.info("inserted book={}", i));
        }
    }

    @Table("books")
    public record Book(Integer id, String name) {}

    @Bean
    @Profile("dev")
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        log.info("db init");
        var initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(
            new ByteArrayResource(("" +
                "DROP TABLE IF EXISTS books;" +
                "CREATE TABLE books (id INT PRIMARY KEY, name VARCHAR(100) NOT NULL);")
                .getBytes())));

        return initializer;
    }
}
