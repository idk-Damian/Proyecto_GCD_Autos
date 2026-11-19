# Sistema de Gestión de Taller Mecánico — Grupo 04

Aplicación web para la gestión completa de un taller automotriz: recepción de vehículos, órdenes de trabajo,
inventario de repuestos, recordatorios de mantenimiento, reportes, auditoría y un portal para los clientes.

Proyecto de la asignatura **Aplicaciones Orientadas a Servicios** — Universidad Técnica de Ambato (FISEI).

## Tecnologías

- Java 17 · Spring Boot 3.2 (Web, Data JPA, Security, Validation, Thymeleaf)
- MySQL 8
- Bootstrap 5 + JavaScript (consumo de la API REST con `fetch`)
- Lombok

## Funcionalidades

| Módulo | Descripción |
|---|---|
| Vehículos | Recepción, ficha, estado en taller (Ingresado → Diagnóstico → Reparación → Listo → Entregado), kilometraje e historial con filtros |
| Órdenes de trabajo | Vista de **activas** ordenadas por urgencia y **finalizadas** aparte; repuestos y mano de obra, mecánico responsable, comprobante en PDF |
| Solicitudes | Citas que piden los clientes desde el portal; se convierten en orden o se rechazan con un mensaje |
| Clientes | Ficha con validación de cédula ecuatoriana y acceso al portal |
| Inventario | Stock de repuestos que se descuenta al usarlos en una orden; alertas de stock bajo |
| Recordatorios | Próximo mantenimiento preventivo (cada N km o M meses) con aviso por WhatsApp o correo |
| Reportes | Ingresos, productividad por mecánico, servicios y repuestos más vendidos; exportable a PDF |
| Auditoría | Registro de quién creó, modificó o eliminó cada dato e inicios de sesión |
| Portal del cliente | Estado de sus vehículos, aprobación de presupuestos, historial, solicitud de citas y perfil |

## Roles

| Rol | Acceso |
|---|---|
| ADMIN | Todo el sistema: usuarios, reportes, auditoría, inventario, asignación de mecánicos |
| MECANICO | Vehículos, órdenes (quedan asignadas a él), solicitudes, clientes, consulta de inventario |
| CLIENTE | Solo su portal (`/portal`) con sus propios vehículos y órdenes |

## Cómo ejecutar

1. Tener MySQL en `localhost:3307` (o ajustar `spring.datasource.url` en `src/main/resources/application.properties`).
   La base `proyecto_soa` se crea sola y las tablas se generan con `ddl-auto=update`.
2. Si MySQL tiene contraseña, definir la variable de entorno `DB_PASSWORD`.
3. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
4. Abrir <http://localhost:8080>.

### Cuentas de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| admin | admin123 | Administrador |
| mecanico | mecanico123 | Mecánico |
| cliente | cliente123 | Cliente (vinculado al primer cliente registrado) |

Los clientes también pueden crear su cuenta en `/registro` con su cédula.
Para ocultar las cuentas demo del login: `app.demo.mostrar-credenciales=false`.

### Configuración del taller

En `application.properties`:

```properties
app.taller.nombre=Taller Mecánico
app.taller.ruc=
app.taller.direccion=Ambato, Ecuador
app.taller.telefono=
app.mantenimiento.intervalo-km=5000
app.mantenimiento.intervalo-meses=6
```

## API REST (resumen)

| Recurso | Endpoints |
|---|---|
| Clientes | `GET/POST /api/clientes`, `PUT/DELETE /api/clientes/{cedula}`, `POST/DELETE /api/clientes/{cedula}/acceso` |
| Vehículos | `GET/POST /api/autos`, `PUT/DELETE /api/autos/{id}`, `PATCH /api/autos/{id}/estado` |
| Órdenes | `GET/POST /api/mantenimientos`, `PUT/DELETE /api/mantenimientos/{id}`, `PATCH /api/mantenimientos/{id}/estado` |
| Repuestos | `GET/POST /api/repuestos`, `PUT/DELETE /api/repuestos/{id}`, `PATCH /api/repuestos/{id}/stock` |
| Solicitudes | `GET /api/solicitudes`, `PATCH /api/solicitudes/{id}/rechazar` |
| Recordatorios | `GET /api/recordatorios` |
| Reportes | `GET /api/reportes/resumen?desde=&hasta=` |
| Auditoría | `GET /api/auditoria` |
| Usuarios | `GET /api/mecanicos`, `GET/POST /api/usuarios`, `DELETE /api/usuarios/{id}` |
| Portal | `GET /api/portal/resumen`, `POST /api/portal/vehiculos`, `POST /api/portal/ordenes/{id}/aprobar`, `POST /api/portal/solicitudes`, `PUT /api/portal/perfil`, `POST /api/portal/password` |

Los errores se devuelven como `{ "mensaje": "...", "errores": { "campo": "..." } }`.

## Estructura

```
src/main/java/com/quinto/taller
├── controller   # Controladores REST y de vistas
├── dto          # Objetos de entrada/salida con validaciones
├── exception    # Excepciones y manejador global
├── model        # Entidades JPA
├── repository   # Repositorios Spring Data
├── security     # Spring Security, datos iniciales, auditoría de sesiones
├── service      # Lógica de negocio
└── util         # Validaciones (cédula, placa)
src/main/resources/templates   # login, registro, panel del taller (index) y portal del cliente
```
