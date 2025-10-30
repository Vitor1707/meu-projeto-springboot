package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.dtos.ProductRequestDTO;
import com.example.Primeiro_Projeto.dtos.ProductResponseDTO;
import com.example.Primeiro_Projeto.dtos.ProductUpdateRequestDTO;
import com.example.Primeiro_Projeto.exceptions.ConflictException;
import com.example.Primeiro_Projeto.exceptions.ResourceNotFoundException;
import com.example.Primeiro_Projeto.model.Product;
import com.example.Primeiro_Projeto.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private  ProductService productService;

    private Product product1;
    private Product product2;

    private ProductRequestDTO request;

    private ProductUpdateRequestDTO requestUpdate;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Product 1", 0.0, List.of());
        product2 = new Product(2L, "Product 2", 0.0, List.of());
        request = new ProductRequestDTO("Novo Product", 0.0);
        requestUpdate = new ProductUpdateRequestDTO("Novo Product", 0.0);
        pageable = PageRequest.of(0, 4, Sort.by("asc", "sort"));
    }

    @Test
    void deveRetornarUmaListDeProductsSeProductsExistirem() {
        List<Product> productsMock = List.of(product1, product2);

        when(productRepository.findAll()).thenReturn(productsMock);

        List<ProductResponseDTO> result = productService.listAllProducts();

        assertNotNull(result);
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());

        verify(productRepository).findAll();
    }

    @Test
    void deveRetornarUmaListaVaziaSeProductsNaoExistirem() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponseDTO> result = productService.listAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findAll();
    }

    @Test
    void deveRetornarUmaPageDeProductsSeProductsExistirem() {

        Page<Product> productsPageMock = new PageImpl<>(List.of(product1, product2));

        when((productRepository.findAll(pageable))).thenReturn(productsPageMock);

        Page<ProductResponseDTO> result = productService.getProductsPaginated(pageable);

        assertNotNull(result);
        assertEquals("Product 1", result.getContent().get(0).getName());
        assertEquals("Product 2", result.getContent().get(1).getName());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmaPageVaziaSeProductsNaoExistirem() {

        Page<Product> productsPageMock = Page.empty();

        when((productRepository.findAll(pageable))).thenReturn(productsPageMock);

        Page<ProductResponseDTO> result = productService.getProductsPaginated(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmProductAoBuscarProductPorIdEIdExistir() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductResponseDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Product 1", result.getName());

        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarProductPorIdENaoExistir() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));

        verify(productRepository).findById(999L);
    }

    @Test
    void deveRetornarUmProductAoBuscarProductPorName() {
        when(productRepository.findByName("Product 1")).thenReturn(Optional.of(product1));

        ProductResponseDTO result = productService.getProductByName("Product 1");

        assertNotNull(result);
        assertEquals("Product 1", result.getName());

        verify(productRepository).findByName("Product 1");
    }

    @Test
    void deveLancarExcecaoAoBuscarProductPorNameENaoExistir() {
        when(productRepository.findByName("Product Inexistente")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductByName("Product Inexistente"));

        verify(productRepository).findByName("Product Inexistente");
    }

    @Test
    void deveSalvarProductSeDadosForemValidos() {

        when(modelMapper.map(request, Product.class)).thenReturn(new Product(null, "Novo Product", 0.0, List.of()));
        when(productRepository.existsByName("Novo Product")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(new Product(1L, "Novo Product", 0.0, List.of()));

        ProductResponseDTO result = productService.saveProduct(request);

        assertNotNull(result);
        assertEquals("Novo Product", result.getName());

        verify(modelMapper).map(request, Product.class);
        verify(productRepository).existsByName("Novo Product");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deveLancarExcecaoAoSalvarProductENameJaEstiverEmUso() {
        when(modelMapper.map(request, Product.class)).thenReturn(new Product(null, "Novo Product", 0.0, List.of()));
        when(productRepository.existsByName("Novo Product")).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> productService.saveProduct(request));

        assertTrue(conflict.getMessage().contains("name"));

        verify(modelMapper).map(request, Product.class);
        verify(productRepository).existsByName("Novo Product");
    }

    @Test
    void deveAtualizarProductSeDadosForemValidos() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.existsByNameAndIdNot("Novo Product", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        ProductResponseDTO result = productService.updateProduct(1L, requestUpdate);

        assertNotNull(result);
        assertEquals("Novo Product", result.getName());

        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameAndIdNot("Novo Product", 1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deveLancarUmaExcecaoAoAtualizarProductEIdNaoForEncontrado() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(999L, requestUpdate));

        verify(productRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoSeAoAtualizarProductONovoNameJaEstiverEmUso() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.existsByNameAndIdNot("Novo Product", 1L)).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> productService.updateProduct(1L, requestUpdate));

        assertTrue(conflict.getMessage().contains("name"));

        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameAndIdNot("Novo Product", 1L);
    }

    @Test
    void deveExcluirUmProductSeIdForValido() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.removeProductById(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoDeletarProductEIdNaoExistir() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.removeProductById(999L));

        verify(productRepository).existsById(999L);
    }
}