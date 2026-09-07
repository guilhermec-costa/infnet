package br.edu.infnet.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
class ProductController {
  private final ProductRepository products;

  ProductController(ProductRepository products) {
    this.products = products;
  }

  @GetMapping
  Flux<Product> all() {
    return products.findAll();
  }

  @GetMapping("/{id}")
  Mono<Product> byId(@PathVariable Long id) {
    return products.findById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Mono<Product> create(@RequestBody NewProduct request) {
    return products.save(new Product(null, request.name(), request.price()));
  }

  record NewProduct(String name, java.math.BigDecimal price) {
  }
}
