package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.dtos.UserUpdateRequestDTO;
import com.example.Primeiro_Projeto.exceptions.ConflictException;
import com.example.Primeiro_Projeto.exceptions.ResourceNotFoundException;
import com.example.Primeiro_Projeto.model.Product;
import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.ProductRepository;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import javax.management.relation.RoleInfoNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    private Product product1;
    private Product product2;

    private UserUpdateRequestDTO requestUpdate;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "User 1", "user1@email.com", "123456", List.of(Role.USER, Role.ADMIN), new ArrayList<>());
        user2 = new User(2L, "User 2", "user2@email.com", "123456", List.of(Role.USER), new ArrayList<>());
        product1 = new Product(1L, "Product 1", 0.0, new ArrayList<>());
        product2 = new Product(2L, "Product 2", 0.0, new ArrayList<>());
        requestUpdate = new UserUpdateRequestDTO("Novo User", "novo_user@email.com", "12345");
        pageable = PageRequest.of(0, 5, Sort.by("asc", "sort"));
    }

    @Test
    void deveRetornarUmaListDeUsersSeUsersExistirem() {
        List<User> usersMock = List.of(user1, user2);

        when(userRepository.findAllByOrderByIdAsc()).thenReturn(usersMock);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals("User 1", result.get(0).getUsername());
        assertEquals("user1@email.com", result.get(0).getEmail());
        assertEquals("User 2", result.get(1).getUsername());
        assertEquals("user2@email.com", result.get(1).getEmail());

        verify(userRepository).findAllByOrderByIdAsc();
    }

    @Test
    void deveRetornarUmaListVaziaSeUsersNaoExistirem() {
        when(userRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        List<UserResponseDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAllByOrderByIdAsc();
    }

    @Test
    void deveRetornarUmaPageDosUsersSeUsersExistirem() {
        Page<User> usersPageMock = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(pageable)).thenReturn(usersPageMock);

        Page<UserResponseDTO> result = userService.getUserPaginated(pageable);

        assertNotNull(result);
        assertEquals("User 1", result.getContent().get(0).getUsername());
        assertEquals("user1@email.com", result.getContent().get(0).getEmail());
        assertEquals("User 2", result.getContent().get(1).getUsername());
        assertEquals("user2@email.com", result.getContent().get(1).getEmail());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmaPageVaziaSeUsersNaoExistirem() {
        Page<User> usersPageMock = new PageImpl<>(List.of());

        when(userRepository.findAll(pageable)).thenReturn(usersPageMock);

        Page<UserResponseDTO> result = userService.getUserPaginated(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmUserAoBuscarPorIdEIdExistir() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        UserResponseDTO result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("User 1", result.getUsername());
        assertEquals("user1@email.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarProductPorIdEIdNaoExistir() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(999L));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveAtualizarUserSeIdForValido() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmailAndIdNot("novo_user@email.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserResponseDTO result = userService.updateUser(1L, requestUpdate);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Novo User", result.getUsername());
        assertEquals("novo_user@email.com", result.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("novo_user@email.com", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarUserPorIdEIdNaoExistir() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, requestUpdate));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUserEEmailJaEstarEmUso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmailAndIdNot("novo_user@email.com", 1L)).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.updateUser(1L, requestUpdate));

        assertTrue(conflict.getMessage().contains("email"));

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("novo_user@email.com", 1L);
    }

    @Test
    void deveAdicionarOProductAoUserSeDadosForemValidos() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(userRepository.save(any(User.class))).thenReturn(user1);
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        UserResponseDTO result = userService.addProductForUser(1L, 1L);

        assertNotNull(result);
        assertEquals("Product 1", result.getProducts().get(0).getName());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarProductAoUserEIdDoUserNaoExistir() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.addProductForUser(999L, 1L));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoAdicionarProductAoUserEIdDoProductNaoExistir() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.addProductForUser(1L, 999L));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoAdicionarProductAoUserEUserJaPossuirEsseProduct() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        user1.getProducts().add(product1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.addProductForUser(1L, 1L));

        assertTrue(conflict.getMessage().contains(product1.getName()));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveRemoverOProductDoUserSeDadosForemValidos() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        user1.getProducts().add(product1);
        product1.getUsers().add(user1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(userRepository.save(any(User.class))).thenReturn(user1);
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        UserResponseDTO result = userService.removeProductFromUser(1L, 1L);

        assertNotNull(result);
        assertTrue(result.getProducts().isEmpty());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deveLancarExcecaoAoRemoverProductDoUserEIdDoUserNaoExistir() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        user1.getProducts().add(product1);
        product1.getUsers().add(user1);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.removeProductFromUser(999L, 1L));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoRemoverProductDoUserEIdDoProductNaoExistir() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        user1.getProducts().add(product1);
        product1.getUsers().add(user1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.removeProductFromUser(1L, 999L));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverProductDoUserEUserJaNaoPossuirOProduct() {
        user1.setProducts(new ArrayList<>());
        product1.setUsers(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.removeProductFromUser(1L, 1L));

        assertTrue(conflict.getMessage().contains(product1.getName()));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveAtualizarUserParaAdminSeDadosForemValidos() {
        user1.setRoles(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserResponseDTO result = userService.promoteToAdmin(1L);

        assertNotNull(result);
        assertEquals("ADMIN", result.getRoles().get(0).getAuthority());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarUserParaAdminEIdNaoExistir() {
        user1.setRoles(new ArrayList<>());

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.promoteToAdmin(999L));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUserParaAdminEEleJaForAdmin() {
        user1.setRoles(new ArrayList<>());

        user1.getRoles().add(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.promoteToAdmin(1L));

        assertTrue(conflict.getMessage().contains("já"));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveRemoverUserDeAdminSeDadosForemValidos() {
        user1.setRoles(new ArrayList<>());

        user1.getRoles().add(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserResponseDTO result = userService.removeFromAdmin(1L);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoRemoverUserDeAdminEIdNaoExistir() {
        user1.setRoles(new ArrayList<>());

        user1.getRoles().add(Role.ADMIN);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.removeFromAdmin(999L));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoRemoverUserDeAdminEEleNaoForAdmin() {
        user1.setRoles(new ArrayList<>());

        user1.getRoles().add(Role.ADMIN);

        user1.getRoles().remove(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.removeFromAdmin(1L));

        assertTrue(conflict.getMessage().contains("não"));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveRemoverUserPorIdSeIdExistir() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.removeUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverUserPorIdEIdNaoExistir() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.removeUser(999L));

        verify(userRepository).existsById(999L);
    }
}