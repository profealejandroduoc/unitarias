package com.pruebas.unitarias.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;

//import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;

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

        // Simula que al guardar la mascota, el repo retorna la mascota con ID
        when(mascotaRepository.save(mascota)).thenReturn(mascotaGuardada);

        // Llama al método a probar
        Mascota resultado = mascotaService.guardarMascota(mascota);

        // Verifica que el resultado sea como esperas
        assertThat(resultado).isEqualTo(mascotaGuardada);
        // assertThat(resultado.getNombre()).isEqualTo("Rex");
        // Verifica que se llamó al repo
        verify(mascotaRepository).save(mascota);
    }

    @Test
    void testListarMascota() {
        Mascota mascota = new Mascota(null, "Rex", "Perro", 5);
        List<Mascota> mascotas = new ArrayList<>();
        mascotas.add(mascota);

        when(mascotaRepository.findAll()).thenReturn(mascotas);

        List<Mascota> resultado = mascotaService.listarMascotas();

        assertThat(mascotas.size()).isEqualTo(resultado.size());
        assertThat(mascotas).contains(mascota);

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
    void testactualizarMascota() {
        Mascota mascotaExistente = new Mascota(1L, "Rex", "Perro", 5);
        Mascota mascotaActualizada = new Mascota(null, "Toby", "Perro", 6);

        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invoc -> invoc.getArgument(0));
        /*
         * thenAnswer(invoc -> invoc.getArgument(0)):
         * devuelve lo mismo que le pasaron como parámetro, porque en este caso debe ser la misma mascota
         * 
         * Es útil cuando el método normalmente devuelve el mismo objeto que recibe,
         * como suele pasar con los métodos save de repositorios JPA.
         */

        Mascota resultado = mascotaService.actualizarMascota(1L, mascotaActualizada);

        assertThat(resultado.getNombre()).isEqualTo("Toby");

        verify(mascotaRepository).findById(1L);
        verify(mascotaRepository).save(mascotaExistente);

    }

}
