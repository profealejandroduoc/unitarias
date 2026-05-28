# 🐾 Guía Paso a Paso: CRUD de Mascotas con Spring Boot y Pruebas Unitarias

## 1. Objetivo del tutorial

Este tutorial tiene como objetivo construir una API REST sencilla para gestionar mascotas utilizando **Spring Boot**, **Spring Data JPA**, **MySQL**, **Lombok**, **JUnit 5**, **Mockito**, **MockMvc** y **H2** para pruebas.

Al finalizar, el estudiante será capaz de:

* Crear una API REST con arquitectura en capas.
* Implementar una entidad, repositorio, servicio y controlador.
* Configurar una base de datos MySQL para ejecución normal.
* Crear pruebas unitarias para la capa de servicio.
* Crear pruebas del controlador usando `@WebMvcTest` y `MockMvc`.
* Crear pruebas de integración usando `@SpringBootTest`, `MockMvc` y H2.
* Documentar la API con Swagger.

---

## 2. Tipos de pruebas que se trabajarán

Antes de comenzar, es importante diferenciar los tipos de pruebas que se usarán en el proyecto.

| Tipo de prueba              | Qué se prueba                                                                         | Herramientas principales           | Usa base de datos real |
| --------------------------- | ------------------------------------------------------------------------------------- | ---------------------------------- | ---------------------- |
| Prueba unitaria de servicio | La lógica de negocio del servicio de forma aislada                                    | JUnit 5 + Mockito                  | No                     |
| Prueba del controlador      | Los endpoints, códigos HTTP y respuestas JSON                                         | `@WebMvcTest` + `MockMvc`          | No                     |
| Prueba de integración       | El flujo completo entre controlador, servicio, repositorio y base de datos en memoria | `@SpringBootTest` + `MockMvc` + H2 | No, usa H2             |

### ¿Por qué es importante esta diferencia?

Porque no todas las pruebas verifican lo mismo.

Una **prueba unitaria** debe enfocarse en una parte pequeña del sistema, por ejemplo, un método del servicio. Para lograrlo, se simulan sus dependencias usando Mockito.

Una **prueba del controlador** verifica que los endpoints respondan correctamente, sin levantar toda la aplicación.

Una **prueba de integración** verifica que varias capas funcionen juntas, pero usando una base de datos en memoria para no depender de MySQL durante la ejecución de los tests.

---

## 3. Creación del proyecto con Spring Initializr

### ¿Por qué?

Spring Initializr permite generar proyectos Spring Boot fácilmente, incorporando las dependencias necesarias desde el inicio.

### Pasos

1. Ingresa a [https://start.spring.io/](https://start.spring.io/)
2. Completa los siguientes campos:

| Campo       | Valor sugerido           |
| ----------- | ------------------------ |
| Project     | Maven                    |
| Language    | Java                     |
| Spring Boot | 3.3.x o superior estable |
| Group       | `com.pruebas`            |
| Artifact    | `unitarias`              |
| Name        | `unitarias`              |
| Java        | 17                       |

1. Agrega las siguientes dependencias:

* Spring Web
* Spring Data JPA
* MySQL Driver
* Lombok
* Spring Boot Starter Test
* H2 Database

1. Haz clic en **GENERATE**.
2. Descomprime el proyecto y ábrelo en tu IDE.

---

## 4. Dependencias principales del proyecto

El archivo `pom.xml` debe incluir las siguientes dependencias principales:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.6.0</version>
    </dependency>
</dependencies>
```

> Nota: Si usas una versión más reciente de Spring Boot, verifica que la versión de Springdoc sea compatible.

---

## 5. Configuración de la base de datos principal

### ¿Por qué?

La aplicación necesita saber a qué base de datos conectarse para guardar los datos cuando se ejecuta normalmente.

En este proyecto se usará **MySQL** para la ejecución real de la aplicación.

### Paso 1: Crear la base de datos

En MySQL, crea la base de datos:

```sql
CREATE DATABASE mascotas_db;
```

### Paso 2: Configurar `application.properties`

En el archivo:

```text
src/main/resources/application.properties
```

agrega la siguiente configuración:

```properties
spring.application.name=unitarias

spring.datasource.url=jdbc:mysql://localhost:3306/mascotas_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
```

> Ajusta el usuario y la contraseña según la configuración de tu equipo.

---

## 6. Configuración de base de datos para pruebas con H2

### ¿Por qué?

Las pruebas no deberían depender de que MySQL esté instalado, iniciado o correctamente configurado.

Para las pruebas de integración usaremos **H2**, una base de datos en memoria que se crea y elimina automáticamente durante la ejecución de los tests.

### Crear archivo de configuración de pruebas

Crea el archivo:

```text
src/test/resources/application-test.properties
```

Agrega el siguiente contenido:

```properties
spring.datasource.url=jdbc:h2:mem:mascotas_test
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

Este archivo será usado solamente cuando una prueba tenga activo el perfil `test`.

---

## 7. Creación del modelo: Entidad Mascota

### ¿Por qué?

El modelo representa los datos principales del sistema. En este caso, cada objeto `Mascota` será una fila en la tabla `mascotas`.

Crea el paquete:

```text
com.pruebas.unitarias.model
```

Dentro del paquete, crea el archivo `Mascota.java`:

```java
package com.pruebas.unitarias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "edad")
    private int edad;
}
```

---

## 8. Creación del repositorio

### ¿Por qué?

El repositorio permite interactuar con la base de datos sin escribir consultas SQL básicas manualmente.

Crea el paquete:

```text
com.pruebas.unitarias.repository
```

Dentro del paquete, crea el archivo `MascotaRepository.java`:

```java
package com.pruebas.unitarias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pruebas.unitarias.model.Mascota;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
}
```

---

## 9. Creación del servicio

### ¿Por qué?

El servicio contiene la lógica de negocio de la aplicación. En este caso, administra las operaciones principales sobre las mascotas.

Crea el paquete:

```text
com.pruebas.unitarias.service
```

Dentro del paquete, crea el archivo `MascotaService.java`:

```java
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

## 10. Creación del controlador REST

### ¿Por qué?

El controlador recibe solicitudes HTTP y responde a clientes como Postman, Swagger, aplicaciones frontend o aplicaciones móviles.

Crea el paquete:

```text
com.pruebas.unitarias.controller
```

Dentro del paquete, crea el archivo `MascotaController.java`:

```java
package com.pruebas.unitarias.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.service.MascotaService;

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

> En este tutorial se usará la ruta base `/api/mascotas` para mantener el ejemplo simple y coherente con las pruebas.

---

## 11. Endpoints disponibles

| Método HTTP | Ruta                 | Descripción                     |
| ----------- | -------------------- | ------------------------------- |
| POST        | `/api/mascotas`      | Crea una nueva mascota          |
| GET         | `/api/mascotas`      | Lista todas las mascotas        |
| GET         | `/api/mascotas/{id}` | Busca una mascota por ID        |
| PUT         | `/api/mascotas/{id}` | Actualiza una mascota existente |
| DELETE      | `/api/mascotas/{id}` | Elimina una mascota por ID      |

---

## 12. Pruebas unitarias del servicio

### ¿Por qué?

Las pruebas unitarias del servicio permiten verificar la lógica de negocio sin conectarse a la base de datos.

Para lograrlo, se simula el repositorio usando Mockito.

Crea el archivo:

```text
src/test/java/com/pruebas/unitarias/service/MascotaServiceTest.java
```

Código sugerido:

```java
package com.pruebas.unitarias.service;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void testEliminarMascota() {
        doNothing().when(mascotaRepository).deleteById(1L);

        mascotaService.eliminarMascota(1L);

        verify(mascotaRepository).deleteById(1L);
    }
}
```

### Conceptos importantes usados en esta prueba

| Concepto                    | Explicación                                                         |
| --------------------------- | ------------------------------------------------------------------- |
| `@Mock`                     | Crea una dependencia simulada                                       |
| `@InjectMocks`              | Inyecta los mocks en la clase que se quiere probar                  |
| `when(...).thenReturn(...)` | Define qué debe devolver un método simulado                         |
| `verify(...)`               | Verifica que un método fue llamado                                  |
| `thenAnswer(...)`           | Permite devolver dinámicamente un valor según el argumento recibido |

---

## 13. Pruebas del controlador con MockMvc

### ¿Por qué?

Estas pruebas permiten verificar que los endpoints respondan correctamente, sin levantar toda la aplicación y sin conectarse a la base de datos.

Aquí se usa:

* `@WebMvcTest`, para probar solamente la capa web.
* `MockMvc`, para simular peticiones HTTP.
* `@MockBean`, para simular el servicio.

Crea el archivo:

```text
src/test/java/com/pruebas/unitarias/controller/MascotaControllerTest.java
```

Código sugerido:

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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Toby")))
                .andExpect(jsonPath("$[1].tipo", is("Gato")));
    }

    @Test
    void testGuardarMascota() throws Exception {
        Mascota nueva = new Mascota(null, "Michi", "Gato", 1);
        Mascota guardada = new Mascota(2L, "Michi", "Gato", 1);

        Mockito.when(mascotaService.guardarMascota(any(Mascota.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nombre").value("Michi"))
                .andExpect(jsonPath("$.tipo").value("Gato"))
                .andExpect(jsonPath("$.edad").value(1));
    }

    @Test
    void testObtenerMascotaPorIdExistente() throws Exception {
        Mascota buscada = new Mascota(2L, "Michi", "Gato", 1);

        Mockito.when(mascotaService.obtenerMascotaPorId(2L)).thenReturn(Optional.of(buscada));

        mockMvc.perform(get("/api/mascotas/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nombre").value("Michi"));
    }

    @Test
    void testObtenerMascotaPorIdNoExistente() throws Exception {
        Mockito.when(mascotaService.obtenerMascotaPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mascotas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarMascota() throws Exception {
        Mascota actualizada = new Mascota(1L, "Rocky", "Perro", 5);

        Mockito.when(mascotaService.actualizarMascota(eq(1L), any(Mascota.class)))
                .thenReturn(actualizada);

        mockMvc.perform(put("/api/mascotas/1")
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

        mockMvc.perform(put("/api/mascotas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mascota)))
                .andExpect(status().isNotFound());
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

## 14. Pruebas de integración del controlador

### ¿Por qué?

Las pruebas de integración permiten comprobar que varias capas de la aplicación funcionan juntas.

En este caso se prueba el flujo:

```text
Controlador → Servicio → Repositorio → Base de datos H2
```

A diferencia de las pruebas unitarias, aquí no se simula el repositorio. Se usa una base de datos real en memoria.

Crea el archivo:

```text
src/test/java/com/pruebas/unitarias/controller/MascotaControllerIT.java
```

Código sugerido:

```java
package com.pruebas.unitarias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebas.unitarias.model.Mascota;
import com.pruebas.unitarias.repository.MascotaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        mascotaRepository.deleteAll();
    }

    @Test
    void testCrearYObtenerMascota() throws Exception {
        Mascota mascota = new Mascota(null, "Max", "Perro", 4);

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mascota)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Max"));

        mockMvc.perform(get("/api/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Max"))
                .andExpect(jsonPath("$[0].tipo").value("Perro"))
                .andExpect(jsonPath("$[0].edad").value(4));
    }

    @Test
    void testEliminarMascota() throws Exception {
        Mascota mascota = new Mascota(null, "Firulais", "Perro", 3);
        Mascota guardada = mascotaRepository.save(mascota);

        mockMvc.perform(delete("/api/mascotas/" + guardada.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/mascotas/" + guardada.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarMascota() throws Exception {
        Mascota mascota = new Mascota(null, "Rocky", "Perro", 2);
        Mascota guardada = mascotaRepository.save(mascota);

        Mascota actualizada = new Mascota(null, "Rocky", "Perro", 5);

        mockMvc.perform(put("/api/mascotas/" + guardada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edad").value(5));
    }
}
```

---

## 15. Ejecutar las pruebas

Para ejecutar todas las pruebas desde la terminal:

```bash
mvn test
```

Si todo está correcto, Maven debería mostrar un resultado similar a:

```text
BUILD SUCCESS
```

---

## 16. Probar la API manualmente con Postman

Con la aplicación ejecutándose, puedes probar los endpoints.

### Crear una mascota

Método:

```text
POST
```

URL:

```text
http://localhost:8080/api/mascotas
```

Body en formato JSON:

```json
{
  "nombre": "Toby",
  "tipo": "Perro",
  "edad": 3
}
```

### Listar mascotas

Método:

```text
GET
```

URL:

```text
http://localhost:8080/api/mascotas
```

### Buscar mascota por ID

Método:

```text
GET
```

URL:

```text
http://localhost:8080/api/mascotas/1
```

### Actualizar mascota

Método:

```text
PUT
```

URL:

```text
http://localhost:8080/api/mascotas/1
```

Body en formato JSON:

```json
{
  "nombre": "Toby",
  "tipo": "Perro",
  "edad": 4
}
```

### Eliminar mascota

Método:

```text
DELETE
```

URL:

```text
http://localhost:8080/api/mascotas/1
```

---

## 17. Documentación de la API con Swagger

### ¿Por qué?

Swagger permite visualizar y probar los endpoints desde el navegador.

Con la aplicación ejecutándose, abre la siguiente URL:

```text
http://localhost:8080/doc/swagger-ui.html
```

Desde esa interfaz podrás revisar y probar los endpoints de la API.

---

## 18. Errores frecuentes

### Error 404 en las pruebas del controlador

Revisa que la ruta usada en el controlador sea la misma que la ruta usada en las pruebas.

En este tutorial se usa:

```java
@RequestMapping("/api/mascotas")
```

Por lo tanto, las pruebas deben usar rutas como:

```java
get("/api/mascotas")
post("/api/mascotas")
put("/api/mascotas/1")
delete("/api/mascotas/1")
```

### Error de conexión a MySQL al ejecutar pruebas

Las pruebas de integración deben usar H2, no MySQL.

Verifica que exista el archivo:

```text
src/test/resources/application-test.properties
```

Y que la prueba de integración tenga:

```java
@ActiveProfiles("test")
```

### Swagger no abre en `/swagger-ui.html`

En este proyecto se configuró Swagger con la ruta:

```properties
springdoc.swagger-ui.path=/doc/swagger-ui.html
```

Por lo tanto, la URL correcta es:

```text
http://localhost:8080/doc/swagger-ui.html
```

---

## 19. Conclusión

Con este proyecto se construyó una API REST sencilla usando Spring Boot y arquitectura en capas.

Además, se implementaron tres niveles de prueba:

* pruebas unitarias del servicio;
* pruebas del controlador;
* pruebas de integración con H2.

Este enfoque permite comprender que probar software no consiste solo en comprobar que la aplicación funciona manualmente, sino en automatizar verificaciones que aseguren el correcto comportamiento del sistema.

---

## 20. Actividad sugerida para estudiantes

Como actividad adicional, modifica el proyecto agregando validaciones a la entidad `Mascota`.

Por ejemplo:

* `nombre` no puede estar vacío;
* `tipo` no puede estar vacío;
* `edad` no puede ser negativa.

Luego agrega pruebas para verificar que las validaciones se cumplan correctamente.
