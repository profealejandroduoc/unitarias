package com.pruebas.unitarias.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;

@Service
public class MascotaService {
    @Autowired
    private MascotaRepository mascotaRepository;

    @SuppressWarnings("null")
    public Mascota guardarMascota(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Mascota> obtenerMascotaPorId(Long id) {
        return mascotaRepository.findById(id);
    }

    @SuppressWarnings("null")
    public Mascota actualizarMascota(Long id, Mascota mascota) {
        Mascota existente = mascotaRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe la mascota"));
        existente.setNombre(mascota.getNombre());
        existente.setTipo(mascota.getTipo());
        existente.setEdad(mascota.getEdad());
        return mascotaRepository.save(existente);
    }

    @SuppressWarnings("null")
    public void eliminarMascota(Long id) {
        mascotaRepository.deleteById(id);
    }

}
