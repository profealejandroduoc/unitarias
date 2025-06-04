package com.pruebas.unitarias.repository;

import com.pruebas.unitarias.model.Mascota;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest  // Levanta solo la capa JPA y una base en memoria
//@EntityScan(basePackages = "com.pruebas.unitarias.model")
//@EnableJpaRepositories(basePackages = "com.pruebas.unitarias.repository")
@SpringBootTest
public class MascotaRepositoryIT {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Test
    void testGuardarYBuscarMascota() {
        Mascota mascota = new Mascota(null, "Firulais", "Perro", 3);
        Mascota guardada = mascotaRepository.save(mascota);

        Optional<Mascota> encontrada = mascotaRepository.findById(guardada.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNombre()).isEqualTo("Firulais");
        assertThat(encontrada.get().getTipo()).isEqualTo("Perro");
        assertThat(encontrada.get().getEdad()).isEqualTo(3);
    }

    @Test
    void testEliminarMascota() {
        Mascota mascota = new Mascota(null, "Michi", "Gato", 2);
        Mascota guardada = mascotaRepository.save(mascota);

        mascotaRepository.deleteById(guardada.getId());
        Optional<Mascota> encontrada = mascotaRepository.findById(guardada.getId());

        assertThat(encontrada).isNotPresent();
    }
}
