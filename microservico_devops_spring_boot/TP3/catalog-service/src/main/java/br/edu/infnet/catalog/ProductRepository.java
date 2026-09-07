package br.edu.infnet.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
}
