# Desarrollo de un sistema de envío de correo con RabbitMQ y Java Spring Boot
## Descripción del problema
Se debe desarrollar un sistema utilizando *Java Spring Boot* y *RabbitMQ* que permita la gestión del envío de correos electrónicos de manera asíncrona mediante un mecanismo de colas.

El sistema debe incluir un *endpoint REST* que reciba la solicitud de envío de correo (incluyendo destinatario, asunto y cuerpo del mensaje) y que encole dicha solicitud en una cola manejada por *RabbitMQ*.

Además, se deben cumplir los siguientes criterios de aceptación:

1.	**Tolerancia a fallos en el servicio de envío de correos**:
Si el servidor SMTP de envío de correos se encuentra caído en el momento de intentar enviar un correo, el sistema debe ser capaz de reintentar el envío posteriormente, sin perder el mensaje encolado.
2.	**Persistencia ante caída del servidor de colas RabbitMQ**:
Los mensajes encolados deben ser persistentes en *RabbitMQ*, de modo que, ante una caída y posterior recuperación del servidor de colas, los correos encolados no se pierdan y puedan ser procesados una vez restaurado el servicio.

## Análisis
### Análisis de requisitos
#### Envío de mensajes:
•	Se pide la creación de endpoint que reciba el destinatario (correo), asunto y cuerpo del mensaje.

•	El correo se debe encolar en un sistema de colas para su envío.
#### Control de errores:
•	En caso de que el sistema SMTP de envío de correos se encuentre caído, se debe reintentar el reenvió al estar disponible.

•	Los mensajes encolados deben ser persistentes en caso de caídas del sistema.

#### Monitoreo:
•	Uso de Loggings para el seguimiento de la actividad del sistema

### Suposiciones
•	El destinatario es una única persona

## Tecnologías usadas
| Tecnología       | Descripción                                                         |
|------------------|---------------------------------------------------------------------|
| JAVA             | V .17                                                               |
| Intellij IDEA    | Entorno de desarrollo                                               |
| Spring Boot      | V.3.4.4                                                             |
| Java Mail Sender | Servicio de envío de correos                                        |
| RabbitMQ         | Sistema de colas                                                    |
| Junit y Mockito  | Para la realización de los tests                                    |
| GitHub           | Repositorio del código y gestión de versiones                       |
| Lombok           | Permite el uso de anotaciones permitiendo generar código más limpio |
| Thymeleaf        | Permite la creación de plantillas para los correos                  |

## Extras
- Nuevo endpoint que permite el envío de correos a una fecha y hora específica
- Uso de plantillas en el correo


