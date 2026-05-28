package com.pruebas.unitarias.service;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @InjectMocks
    private MascotaService mascotaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGuardarMascota() {
        Mascota mascota = new Mascota(null, "Rex", "Perro", 5);
        Mascota mascotaGuardada = new Mascota(1L, "Rex", "Perro", 5);

        when(mascotaRepository.save(mascota)).thenReturn(mascotaGuardada);

        Mascota resultado = mascotaService.guardarMascota(mascota);

        assertThat(resultado).isEqualTo(mascotaGuardada);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Rex");

        verify(mascotaRepository).save(mascota);
    }

    @Test
    void testListarMascotas() {
        Mascota mascota = new Mascota(1L, "Rex", "Perro", 5);
        List<Mascota> mascotas = new ArrayList<>();
        mascotas.add(mascota);

        when(mascotaRepository.findAll()).thenReturn(mascotas);

        List<Mascota> resultado = mascotaService.listarMascotas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado).contains(mascota);

        verify(mascotaRepository).findAll();
    }

    @Test
    void testObtenerMascotaPorId() {
        Mascota mascota = new Mascota(1L, "Rex", "Perro", 5);

        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));

        Optional<Mascota> resultado = mascotaService.obtenerMascotaPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Rex");

        verify(mascotaRepository).findById(1L);
    }

    @Test
    void testEliminarMascota() {
        doNothing().when(mascotaRepository).deleteById(1L);

        mascotaService.eliminarMascota(1L);

        verify(mascotaRepository).deleteById(1L);
    }

    @Test
    void testActualizarMascota() {
        Mascota mascotaExistente = new Mascota(1L, "Rex", "Perro", 5);
        Mascota mascotaActualizada = new Mascota(null, "Toby", "Perro", 6);

        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Mascota resultado = mascotaService.actualizarMascota(1L, mascotaActualizada);

        assertThat(resultado.getNombre()).isEqualTo("Toby");
        assertThat(resultado.getTipo()).isEqualTo("Perro");
        assertThat(resultado.getEdad()).isEqualTo(6);

        verify(mascotaRepository).findById(1L);
        verify(mascotaRepository).save(mascotaExistente);
    }
}
