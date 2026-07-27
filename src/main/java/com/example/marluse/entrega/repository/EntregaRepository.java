package com.example.marluse.entrega.repository;

import com.example.marluse.entrega.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, String> {
}
