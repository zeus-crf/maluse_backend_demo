package com.example.marluse.security.repository;

import com.example.marluse.security.model.RefreshToken;
import com.example.marluse.security.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);

    List<RefreshToken> findAllByUsuario(Usuario usuario);
}
