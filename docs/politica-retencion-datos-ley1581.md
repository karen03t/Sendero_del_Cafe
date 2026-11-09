\# Política de tratamiento de datos personales

\## Sendero del Café — Aplicación de Turismo para el Eje Cafetero

\### Ley 1581 de 2012 · Trazabilidad: SCRUM-36, RNF-02, D-08



\---



\## 1. ¿Qué datos recopilamos?



| Dato | Tabla en BD | Obligatorio | Finalidad |

|------|------------|-------------|-----------|

| Nombre completo | `turista.nombre\_completo` | Sí | Identificar al usuario en la app |

| Correo electrónico | `turista.correo` | Sí | Inicio de sesión y comunicación |

| Contraseña (hash) | `turista.password\_hash` | Sí (local) | Autenticación segura |

| Intereses turísticos | `turista\_interes` | Sí (mín. 1) | Personalizar rutas y recomendaciones |

| Ubicación (al usar la app) | No se almacena | No | Calcular rutas desde la posición actual |

| Rutas guardadas | `ruta\_guardada` | No | Permitir al turista guardar sus rutas |



\## 2. ¿Para qué usamos los datos?



\- Crear y gestionar la cuenta del turista

\- Personalizar las recomendaciones de sitios turísticos según los intereses seleccionados

\- Generar y guardar rutas turísticas personalizadas

\- Mejorar la experiencia de uso de la aplicación



\## 3. ¿Qué NO hacemos con los datos?



\- \*\*No vendemos\*\* datos personales a terceros

\- \*\*No compartimos\*\* información con fines publicitarios

\- \*\*No rastreamos\*\* la ubicación en segundo plano — solo se consulta cuando el usuario solicita una ruta

\- \*\*No almacenamos\*\* la ubicación del usuario en la base de datos

\- \*\*No enviamos\*\* comunicaciones comerciales sin autorización



\## 4. ¿Cómo protegemos los datos en la base de datos?



\### 4.1 Autenticación

\- Las contraseñas se almacenan como hash (nunca en texto plano)

\- El campo `password\_hash` nunca se expone en las respuestas de la API

\- Soporte para autenticación con proveedores externos (Google, Apple) sin almacenar contraseña



\### 4.2 Consentimiento explícito

\- El campo `turista.acepta\_tratamiento` debe ser `1` para completar el registro (restricción CHECK en V1)

\- Se registra la `fecha\_consentimiento` del momento exacto de la autorización

\- Sin consentimiento, el sistema rechaza el registro



\### 4.3 Correo único

\- Restricción UNIQUE sobre `turista.correo` impide duplicados (RN-06)



\### 4.4 Auditoría de cambios

\- Toda modificación a los lugares turísticos queda registrada en `auditoria\_lugar` con fecha, operador y dato modificado (RN-07)



\## 5. Retención de datos



| Situación | Acción | Plazo |

|-----------|--------|-------|

| Cuenta activa en uso | Se retienen los datos | Indefinido |

| Sin actividad | Se marca como INACTIVO | Después de 12 meses |

| Cuenta inactiva sin reactivación | Se anonimiza | 6 meses después de inactivarse |

| Registros de auditoría | Se conservan para trazabilidad | 3 años desde el evento |



\## 6. Eliminación de datos (derecho de supresión, Art. 8 Ley 1581)



El turista puede solicitar la eliminación de su cuenta en cualquier momento. El proceso es:



1\. Se anonimiza el nombre → "Usuario eliminado"

2\. Se elimina el correo electrónico

3\. Se eliminan los registros de `turista\_interes`

4\. Las rutas guardadas se desvinculan (`ON DELETE CASCADE`)

5\. Se registra la `fecha\_eliminacion` para auditoría

6\. Después de 6 meses, se ejecuta el hard delete definitivo



\### Soporte en BD:

\- Columna `turista.fecha\_ultima\_actividad` — detectar cuentas inactivas

\- Columna `turista.fecha\_eliminacion` — rastrear solicitudes de eliminación

\- Índice `idx\_turista\_ultima\_actividad` — consulta eficiente de inactividad

\- Índice `idx\_turista\_eliminacion` — procesar eliminaciones pendientes



\## 7. Reactivación



\- Un turista INACTIVO puede reactivar su cuenta iniciando sesión dentro del período de retención

\- Una vez anonimizados, los datos \*\*no son recuperables\*\*



\## 8. Aviso al usuario



Al registrarse, la aplicación debe mostrar:



> "Al crear tu cuenta, autorizas el tratamiento de tus datos personales (nombre, correo e intereses) conforme a la Ley 1581 de 2012. Tus datos se usan exclusivamente para personalizar tu experiencia de viaje en el Eje Cafetero. No compartimos tu información con terceros. Puedes solicitar la eliminación de tu cuenta y tus datos en cualquier momento desde la configuración de tu perfil."



\## 9. Responsable



\- El líder de Datos es responsable de implementar y verificar estas políticas

\- Cualquier cambio debe ser aprobado por el equipo y documentado en Jira

