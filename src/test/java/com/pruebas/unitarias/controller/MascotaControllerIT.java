package com.pruebas.unitarias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MascotaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        mascotaRepository.deleteAll(); // Limpiar la base antes de cada test
    }

    @Test
    void testCrearYObtenerMascota() throws Exception {
        Mascota mascota = new Mascota(null, "Max", "Perro", 4);

        // Crear mascota (POST)
        mockMvc.perform(post("/api/v1/mascotas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mascota)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Max"))
                .andReturn().getResponse().getContentAsString();

        // Listar mascotas (GET)
        mockMvc.perform(get("/api/v1/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Max"))
                .andExpect(jsonPath("$[0].tipo").value("Perro"))
                .andExpect(jsonPath("$[0].edad").value(4));
    }

    @Test
    void testEliminarMascota() throws Exception {
        // Primero crea la mascota
        Mascota mascota = new Mascota(null, "Firulais", "Perro", 3);
        Mascota guardada = mascotaRepository.save(mascota);

        // Ahora la elimina por ID (DELETE)
        mockMvc.perform(delete("/api/v1/mascotas/" + guardada.getId()))
                .andExpect(status().isNoContent());

        // Comprueba que ya no existe
        mockMvc.perform(get("/api/v1/mascotas/" + guardada.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarMascota() throws Exception {
        // Crear mascota
        Mascota mascota = new Mascota(null, "Rocky", "Perro", 2);
        Mascota guardada = mascotaRepository.save(mascota);

        // Modificar y enviar el PUT
        Mascota actualizada = new Mascota(null, "Rocky", "Perro", 5);

        mockMvc.perform(put("/api/v1/mascotas/" + guardada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edad").value(5));
    }
}
