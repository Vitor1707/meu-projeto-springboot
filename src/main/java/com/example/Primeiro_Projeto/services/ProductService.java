package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.config.LogMessages;
import com.example.Primeiro_Projeto.dtos.ProductRequestDTO;
import com.example.Primeiro_Projeto.dtos.ProductResponseDTO;
import com.example.Primeiro_Projeto.dtos.ProductUpdateRequestDTO;
import com.example.Primeiro_Projeto.exceptions.ConflictException;
import com.example.Primeiro_Projeto.exceptions.ResourceNotFoundException;
import com.example.Primeiro_Projeto.model.Product;
import com.example.Primeiro_Projeto.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Cacheable(value = "allProducts")
    public List<ProductResponseDTO> listAllProducts() {
        log.info(LogMessages.RESOURCE_LIST_ALL + " - " + LogMessages.CACHE_SAVED, "products");
        log.info(LogMessages.DATABASE_QUERY);
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    @Cacheable(value = "productsPage", key = "#pageable.getPageNumber" + " - " + "#pageable.getPageSize")
    public Page<ProductResponseDTO> getProductsPaginated(Pageable pageable) {
        log.info(LogMessages.RESOURCE_LIST_ALL + " - " + LogMessages.CACHE_SAVED, "products");
        log.info(LogMessages.DATABASE_QUERY);
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.map(ProductResponseDTO::new);
    }

    @Cacheable(value = "productId", key = "#id")
    public ProductResponseDTO getProductById(Long id) {
        log.info(LogMessages.RESOURCE_FIND_BY_FIELD + " - " + LogMessages.CACHE_SAVED, "product", "id");
        return productRepository.findById(id)
                .map(ProductResponseDTO::new)
                .orElseThrow(() -> {
                    log.warn(LogMessages.RESOURCE_NOT_FOUND, "product", "id", id);
                    return new ResourceNotFoundException("Product", id);
                });
    }

    @Cacheable(value = "productName", key = "#id")
    public ProductResponseDTO getProductByName(String name) {
        log.info(LogMessages.RESOURCE_FIND_BY_FIELD + " - " + LogMessages.CACHE_SAVED, "product", "name");
        return productRepository.findByName(name)
                .map(ProductResponseDTO::new)
                .orElseThrow(() -> {
                    log.warn(LogMessages.RESOURCE_NOT_FOUND, "product", "name", name);
                    return new ResourceNotFoundException("Product", "name", name);
                });
    }

    @CacheEvict(value = {"productName", "productId", "allProducts", "productsPage"}, key = "#id")
    public ProductResponseDTO updateProduct(Long id, ProductUpdateRequestDTO requestUpdate) {
        log.info(LogMessages.RESOURCE_UPDATE + " - " + LogMessages.CACHE_CLEANING, "product", "id", "updateProduct");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(LogMessages.RESOURCE_NOT_FOUND, "product", "id", id);
                    return new ResourceNotFoundException("Product", id);
                });

        ProductResponseDTO response = updateField(product, requestUpdate);
        log.info(LogMessages.OPERATION_SUCCESS, "updateProduct");
        return response;
    }

    @CacheEvict(value = {"allProducts", "productsPage"}, allEntries = true)
    public ProductResponseDTO saveProduct(ProductRequestDTO request) {
        log.info(LogMessages.RESOURCE_CREATE + " - " + LogMessages.CACHE_CLEANING, "product", "saveProduct");
        Product product = modelMapper.map(request, Product.class);

        if(productRepository.existsByName(product.getName())) {
            log.warn(LogMessages.FIELD_CONFLICT, "name", product.getName());
            throw new ConflictException("name", product.getName());
        }

        Product productSaved = productRepository.save(product);
        log.info(LogMessages.OPERATION_SUCCESS, "saveProduct");
        return new ProductResponseDTO(productSaved);
    }

    @CacheEvict(value = {"productName", "productId", "allProducts", "productsPage"}, key = "#id")
    public void removeProductById(Long id) {
        log.info(LogMessages.RESOURCE_DELETE + " - " + LogMessages.CACHE_CLEANING, "product", "id", "removeProductById");
        if(!productRepository.existsById(id)) {
            log.warn(LogMessages.RESOURCE_NOT_FOUND, "product", "id", id);
            throw new ResourceNotFoundException("Product", id);
        }

        productRepository.deleteById(id);
    }

    private ProductResponseDTO updateField(Product product, ProductUpdateRequestDTO requestUpdate) {
        if (
                requestUpdate.getName() != null &&
                        !requestUpdate.getName().isEmpty() &&
                        !requestUpdate.getName().equalsIgnoreCase(product.getName())
        ) {
            if (productRepository.existsByNameAndIdNot(requestUpdate.getName(), product.getId())) {
                log.warn(LogMessages.FIELD_CONFLICT, "name", requestUpdate.getName());
                throw new ConflictException("name", requestUpdate.getName());
            }
            log.info(LogMessages.FIELD_UPDATE, "name", requestUpdate.getName());
            product.setName(requestUpdate.getName());
        }

        if (
                requestUpdate.getPrice() != null &&
                        !requestUpdate.getPrice().equals(product.getPrice())
        ) {

            log.info(LogMessages.FIELD_UPDATE, "price", requestUpdate.getPrice());
            product.setPrice(requestUpdate.getPrice());
        }

        Product productUpdate = productRepository.save(product);
        return new ProductResponseDTO(productUpdate);
    }
}