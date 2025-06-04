# 🐾 Guía Paso a Paso: CRUD de Mascotas con Spring Boot

## 1. Creación del proyecto con Spring Initializr

**¿Por qué?**
Spring Initializr es una web que permite generar proyectos Spring Boot fácilmente y con las dependencias necesarias.

**Pasos:**

1. Ingresa a [https://start.spring.io/](https://start.spring.io/)
2. Completa los campos:

   * **Project:** Maven
   * **Language:** Java
   * **Group:** com.pruebas
   * **Artifact:** unitarias
   * **Java:** 17 (o superior)
3. Haz clic en **Add dependencies** y agrega:

   * Spring Web
   * Spring Data JPA
   * MySQL Driver (o H2 para pruebas en memoria)
   * Lombok
   * Spring Boot Starter Test
4. Haz clic en **GENERATE** para descargar el proyecto.
5. Descomprime el ZIP y ábrelo en tu IDE favorito (IntelliJ, Eclipse, VSCode, etc).

---

## 2. Configuración de la Base de Datos

**¿Por qué?**
Tu aplicación necesita saber a qué base de datos conectarse para guardar los datos de las mascotas.

**Pasos:**

* Crea la base de datos `mascotas_db` en tu gestor de MySQL.
* Abre el archivo `src/main/resources/application.properties` y agrega:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mascotas_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

*(Ajusta el usuario y contraseña si es necesario)*

---

## 3. Creación del Modelo (Entidad Mascota)

**¿Por qué?**
El modelo representa los datos de tu sistema; cada objeto Mascota será una fila en la tabla `mascotas`.

**Pasos:**

* Crea el paquete: `com.pruebas.unitarias.model`
* Agrega el archivo `Mascota.java`:

```java
package com.pruebas.unitarias.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mascotas")
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    private int edad;
}
```

---

## 4. Creación del Repositorio

**¿Por qué?**
El repositorio gestiona la interacción con la base de datos, usando métodos listos para guardar, buscar, eliminar, etc.

**Pasos:**

* Crea el paquete: `com.pruebas.unitarias.repository`
* Agrega el archivo `MascotaRepository.java`:

```java
package com.pruebas.unitarias.repository;

import com.pruebas.unitarias.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {}
```

---

## 5. Creación del Servicio

**¿Por qué?**
El servicio contiene la lógica de negocio y controla qué puede hacer tu aplicación.

**Pasos:**

* Crea el paquete: `com.pruebas.unitarias.service`
* Agrega el archivo `MascotaService.java`:

```java
package com.pruebas.unitarias.service;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {
    @Autowired
    private MascotaRepository mascotaRepository;

    public Mascota guardarMascota(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    public Optional<Mascota> obtenerMascotaPorId(Long id) {
        return mascotaRepository.findById(id);
    }

    public Mascota actualizarMascota(Long id, Mascota mascota) {
        Mascota existente = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la mascota"));
        existente.setNombre(mascota.getNombre());
        existente.setTipo(mascota.getTipo());
        existente.setEdad(mascota.getEdad());
        return mascotaRepository.save(existente);
    }

    public void eliminarMascota(Long id) {
        mascotaRepository.deleteById(id);
    }
}
```

---

## 6. Creación del Controlador REST

**¿Por qué?**
El controlador recibe las solicitudes HTTP y responde a clientes como Postman, frontends, etc.

**Pasos:**

* Crea el paquete: `com.pruebas.unitarias.controller`
* Agrega el archivo `MascotaController.java`:

```java
package com.pruebas.unitarias.controller;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @PostMapping
    public Mascota crearMascota(@RequestBody Mascota mascota) {
        return mascotaService.guardarMascota(mascota);
    }

    @GetMapping
    public List<Mascota> obtenerTodas() {
        return mascotaService.listarMascotas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerPorId(@PathVariable Long id) {
        return mascotaService.obtenerMascotaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(@PathVariable Long id, @RequestBody Mascota mascota) {
        try {
            Mascota actualizada = mascotaService.actualizarMascota(id, mascota);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 7. Pruebas Unitarias del Servicio

**¿Por qué?**
Permiten asegurar que los métodos del servicio funcionan bien de forma aislada.

**Pasos:**

* Crea el archivo `MascotaServiceTest.java` en el paquete de servicio.

```java
package com.pruebas.unitarias.service;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class MascotaServiceTest {

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
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(mascotaRepository).save(mascota);
    }

    @Test
    void testListarMascotas() {
        Mascota m1 = new Mascota(1L, "Rex", "Perro", 5);
        Mascota m2 = new Mascota(2L, "Michi", "Gato", 2);
        when(mascotaRepository.findAll()).thenReturn(Arrays.asList(m1, m2));

        List<Mascota> resultado = mascotaService.listarMascotas();
        assertThat(resultado).hasSize(2).contains(m1, m2);
        verify(mascotaRepository).findAll();
    }

    @Test
    void testObtenerMascotaPorId() {
        Mascota m1 = new Mascota(1L, "Rex", "Perro", 5);
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(m1));

        Optional<Mascota> resultado = mascotaService.obtenerMascotaPorId(1L);
        assertThat(resultado).isPresent().contains(m1);
        verify(mascotaRepository).findById(1L);
    }

    @Test
    void testEliminarMascota() {
        doNothing().when(mascotaRepository).deleteById(1L);
        mascotaService.eliminarMascota(1L);
        verify(mascotaRepository).deleteById(1L);
    }
}
```

---

## 8. Pruebas Unitarias del Controlador

**¿Por qué?**
Estas pruebas verifican que los endpoints del controlador funcionan bien y que el controlador delega correctamente en el servicio, usando MockMvc y simulando el servicio.

**Pasos:**

* Crea el archivo `MascotaControllerTest.java` en el paquete `controller`.

```java
package com.pruebas.unitarias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.service.MascotaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MascotaController.class)
class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$[0].nombre").value("Toby"))
                .andExpect(jsonPath("$[1].tipo").value("Gato"));
    }

    @Test
    void testCrearMascota() throws Exception {
        Mascota nueva = new Mascota(null, "Toby", "Perro", 3);
        Mascota guardada = new Mascota(1L, "Toby", "Perro", 3);

        Mockito.when(mascotaService.guardarMascota(any(Mascota.class)))
                .thenReturn(guardada);

        mockMvc.perform(post("/api/mascotas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Toby"));
    }

    @Test
    void testObtenerPorIdExistente() throws Exception {
        Mascota m = new Mascota(1L, "Toby", "Perro", 3);
        Mockito.when(mascotaService.obtenerMascotaPorId(1L)).thenReturn(Optional.of(m));

        mockMvc.perform(get("/api/mascotas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Toby"));
    }

    @Test
    void testObtenerPorIdNoExistente() throws Exception {
        Mockito.when(mascotaService.obtenerMascotaPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mascotas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarMascota() throws Exception {
        Mascota actualizada = new Mascota(1L, "Rocky", "Perro", 5);
        Mockito.when(mascotaService.actualizarMascota(eq(1L), any(Mascota.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/mascotas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Rocky"))
                .andExpect(jsonPath("$.edad").value(5));
    }

    @Test
    void testEliminarMascota() throws Exception {
        Mockito.doNothing().when(mascotaService).eliminarMascota(1L);

        mockMvc.perform(delete("/api/mascotas/1"))
                .andExpect(status().isNoContent());
    }
}
```

---

## 9. Pruebas de Integración del Controlador

**¿Por qué?**
Permiten probar toda la aplicación de extremo a extremo (controlador, servicio y repositorio, usando la base de datos en memoria).

**Pasos:**

* Crea el archivo `MascotaControllerIT.java` en el paquete `controller`.

```java
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MascotaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        mascotaRepository.deleteAll();
    }

    @Test
    void testCrearYObtenerMascota() throws Exception {
        Mascota mascota = new Mascota(null, "Max", "Perro", 4);

        mockMvc.perform(post("/api/mascotas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mascota)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Max"));

        mockMvc.perform(get("/api/mascotas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Max"));
    }
}
```

---

## 10. Documentación de la API con Swagger

**¿Por qué?**
Swagger permite documentar y probar tus endpoints desde el navegador de forma interactiva.

**Pasos:**

* Agrega esta dependencia en tu `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

* Corre tu aplicación y abre:

  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

* Verás la documentación de tu API y podrás probar cada endpoint.

---

## 11. ¡Listo!

Tu proyecto está completo:

* Arquitectura en capas (modelo, repositorio, servicio, controlador)
* Pruebas unitarias de servicio y controlador
* Pruebas de integración de controlador
* Documentación automática con Swagger

