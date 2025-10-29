package com.example.Primeiro_Projeto.controller;

import com.example.Primeiro_Projeto.config.LogMessages;
import com.example.Primeiro_Projeto.dtos.ProductRequestDTO;
import com.example.Primeiro_Projeto.dtos.ProductResponseDTO;
import com.example.Primeiro_Projeto.dtos.ProductUpdateRequestDTO;
import com.example.Primeiro_Projeto.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info(" GET /api/products/all - " + LogMessages.RESOURCE_LIST_ALL, "products");
        List<ProductResponseDTO> response = productService.listAllProducts();
        log.info(" GET /api/products/all - " + LogMessages.OPERATION_SUCCESS, "getAllProducts");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info(" GET /api/products - " + LogMessages.RESOURCE_LIST_ALL, "products");
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ProductResponseDTO> response = productService.getProductsPaginated(pageable);
        log.info(" GET /api/products - " + LogMessages.OPERATION_SUCCESS, "getProductsPaginated");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProductResponseDTO> findProductById(@PathVariable Long id) {
        log.info(" GET /api/products/id/{} - " + LogMessages.RESOURCE_FIND_BY_FIELD, id, "product", "id");
        ProductResponseDTO response = productService.getProductById(id);
        log.info(" GET /api/products/id/{} - " + LogMessages.OPERATION_SUCCESS, id, "findProductById");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProductResponseDTO> findProductByName(@PathVariable String name) {
        log.info(" GET /api/products/name/{} - " + LogMessages.RESOURCE_FIND_BY_FIELD, name, "product", "name");
        ProductResponseDTO response = productService.getProductByName(name);
        log.info(" GET /api/products/name/{} - " + LogMessages.OPERATION_SUCCESS, name, "findProductByName");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> saveProduct(@RequestBody ProductRequestDTO request) {
        log.info(" POST /api/products - " + LogMessages.RESOURCE_CREATE, "product");
        ProductResponseDTO response = productService.saveProduct(request);
        log.info(" POST /api/products - " + LogMessages.OPERATION_SUCCESS, "saveProduct");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequestDTO requestUpdate) {
        log.info(" PUT /api/products/{}/update - " + LogMessages.RESOURCE_UPDATE, id, "product", "id");
        ProductResponseDTO response = productService.updateProduct(id, requestUpdate);
        log.info(" PUT /api/products/{}/update - " + LogMessages.OPERATION_SUCCESS, id, "updateProduct");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeProductById(@PathVariable Long id) {
        log.info(" DELETE /api/users/{} - " + LogMessages.RESOURCE_DELETE, id, "product", "id");
        productService.removeProductById(id);
        log.info(" DELETE /api/users/{} - " + LogMessages.OPERATION_SUCCESS, id, "removeProductById");
        return ResponseEntity.noContent().build();
    }
}