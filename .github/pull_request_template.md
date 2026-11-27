## Qué hace este PR

<!-- Describe el cambio en una o dos frases. -->

## Trazabilidad

- Issue de Jira: SCRUM-XX
- RF / CA relacionados:

## Checklist antes de pedir revisión

- [ ] La rama sale de `develop` y está actualizada (`git rebase origin/develop`).
- [ ] `mvn -B package` compila sin errores.
- [ ] `mvn -B test` pasa.
- [ ] Si agregué una migración, es un archivo nuevo (`V5`, `V6`…) y **no** edité una ya aplicada.
- [ ] No hay contraseñas, tokens ni secretos en el código: todo va por `.env` / variables de entorno.
- [ ] Los endpoints nuevos tienen pruebas o evidencia en Postman.
- [ ] El commit y el título del PR llevan el ID de Jira (`SCRUM-XX`).

## Evidencia

<!-- Capturas de Postman, salida de pruebas, EXPLAIN, etc. -->
