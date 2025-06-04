package com.pruebas.unitarias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.service.MascotaService;

//import io.swagger.v3.oas.models.media.MediaType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MascotaController.class)
public class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private MascotaService mascotaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testObtenerTodas() throws Exception {
        Mascota m1 = new Mascota(1L, "Toby", "Perro", 3);
        Mascota m2 = new Mascota(2L, "Michi", "Gato", 1);

        Mockito.when(mascotaService.listarMascotas()).thenReturn(Arrays.asList(m1, m2));

        mockMvc.perform(get("/api/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Toby")))
                .andExpect(jsonPath("$[1].tipo", is("Gato")));
    }

    @Test
    void testGuardarMascota() throws Exception {
        Mascota nueva = new Mascota(2L, "Michi", "Gato", 1);
        Mascota guardada = new Mascota(2L, "Michi", "Gato", 1);

        Mockito.when(mascotaService.guardarMascota(nueva)).thenReturn(guardada);

        mockMvc.perform(
                post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk());

    }

    @Test
    void testObtenerMascotaPorId() throws Exception {
        Mascota buscada = new Mascota(2L, "Michi", "Gato", 1);
        // Mascota guardada = new Mascota(2L, "Michi", "Gato", 1);

        Mockito.when(mascotaService.obtenerMascotaPorId(2L)).thenReturn(Optional.of(buscada));

        mockMvc.perform(
                get("/api/mascotas/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buscada)))
                .andExpect(status().isOk());

    }

    @Test
    void testActualizarMascota() throws Exception {
        Mascota actualizada = new Mascota(1L, "Rocky", "Perro", 5);

        Mockito.when(mascotaService.actualizarMascota(eq(1L), any(Mascota.class))).thenReturn(actualizada);

        mockMvc.perform(
                put("/api/mascotas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Rocky"))
                .andExpect(jsonPath("$.tipo").value("Perro"))
                .andExpect(jsonPath("$.edad").value(5));
    }

    @Test
    void testActualizarMascotaInexistente() throws Exception {
        Mascota mascota = new Mascota(null, "Ghost", "Gato", 2);
        Mockito.when(mascotaService.actualizarMascota(eq(99L), any(Mascota.class)))
                .thenThrow(new RuntimeException("No existe la mascota"));

        mockMvc.perform(
                put("/api/mascotas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mascota)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarMascota() throws Exception {
        Mockito.doNothing().when(mascotaService).eliminarMascota(1L);

        mockMvc.perform(
                delete("/api/mascotas/1"))
                .andExpect(status().isNoContent());
    }

}
