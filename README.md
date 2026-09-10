# Proyecto 1 - Sistema de Reservas de Salas de Estudio Universitarias

## Descripción

Este proyecto implementa una simulación de un sistema de reservas de salas
de estudio universitarias.

El sistema busca controlar las solicitudes concurrentes de estudiantes que
intentan reservar una misma sala y evitar conflictos cuando la sala ya está
siendo utilizada.

Como parte de la implementación se utiliza una estructura de datos Queue
(cola) desarrollada manualmente utilizando Java Generics, sin utilizar
`java.util.Queue`.

La cola permite administrar estudiantes que deben esperar cuando una sala
no está disponible.

---

## Contexto del proyecto

El sistema está pensado para la Oficina de Bienestar Universitario, encargada
de administrar las salas de estudio grupales de una biblioteca universitaria.

Durante las horas de mayor demanda, varios estudiantes pueden intentar
reservar la misma sala simultáneamente.

El sistema utiliza una cola FIFO (First-In, First-Out) para organizar las
solicitudes que no pueden ser atendidas inmediatamente.

---

## Estructura de datos

La estructura implementada es:

**Queue<T>**

La cola utiliza nodos enlazados mediante la clase:

**Node<T>**

### Operaciones principales

| Operación | Complejidad |
|---|---|
| enqueue | O(1) |
| dequeue | O(1) |
| peek | O(1) |
| isEmpty | O(1) |
| size | O(1) |
| clear | O(1) |

La implementación mantiene referencias al primer y último nodo para lograr
operaciones de inserción y eliminación en tiempo constante.

---

## Programación concurrente

La clase `ReservationSimulator` utiliza varios hilos (`Thread`) para simular
estudiantes realizando solicitudes de reserva simultáneamente.

Cuando la sala está disponible, el primer estudiante puede obtenerla.

Cuando la sala está ocupada, los estudiantes restantes son agregados a la
cola de espera.

La cola mantiene el orden FIFO.

Cuando la sala se libera, el primer estudiante de la cola recibe la siguiente
oportunidad de reserva.

---

## Pruebas

El proyecto utiliza JUnit para verificar el funcionamiento de la estructura
`Queue`.

Actualmente se ejecutan 6 pruebas:

- Inserción de elementos.
- Eliminación de elementos.
- Consulta del primer elemento.
- Verificación de cola vacía.
- Verificación del tamaño.
- Limpieza de la cola.

Resultado actual:

```text
Tests run: 6
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS