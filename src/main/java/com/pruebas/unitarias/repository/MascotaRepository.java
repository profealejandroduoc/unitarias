package com.pruebas.unitarias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pruebas.unitarias.model.Mascota;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long>{
    
}
