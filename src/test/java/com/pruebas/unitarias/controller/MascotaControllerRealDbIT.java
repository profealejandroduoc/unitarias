package com.pruebas.unitarias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("real-db")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("realdbtest")
class MascotaControllerRealDbIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MascotaRepository mascotaRepository;

    private final List<Long> createdIds = new ArrayList<>();

    @AfterEach
    void cleanCreatedRows() {
        for (Long id : createdIds) {
            if (mascotaRepository.existsById(id)) {
                mascotaRepository.deleteById(id);
            }
        }
        createdIds.clear();
    }

    @Test
    void testFlujoCrudConMysqlReal() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Mascota nueva = new Mascota(null, "RealDB-" + suffix, "Perro", 4);

        MvcResult resultadoCreacion = mockMvc.perform(post("/api/v1/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value(nueva.getNombre()))
                .andReturn();

        Mascota creada = objectMapper.readValue(
                resultadoCreacion.getResponse().getContentAsString(),
                Mascota.class);
        createdIds.add(creada.getId());

        mockMvc.perform(get("/api/v1/mascotas/" + creada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nueva.getNombre()))
                .andExpect(jsonPath("$.tipo").value("Perro"))
                .andExpect(jsonPath("$.edad").value(4));

        Mascota actualizada = new Mascota(null, "RealDB-" + suffix, "Gato", 5);

        mockMvc.perform(put("/api/v1/mascotas/" + creada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(creada.getId()))
                .andExpect(jsonPath("$.tipo").value("Gato"))
                .andExpect(jsonPath("$.edad").value(5));

        mockMvc.perform(delete("/api/v1/mascotas/" + creada.getId()))
                .andExpect(status().isNoContent());
        createdIds.removeIf(id -> id.equals(creada.getId()));

        mockMvc.perform(get("/api/v1/mascotas/" + creada.getId()))
                .andExpect(status().isNotFound());
    }
}
